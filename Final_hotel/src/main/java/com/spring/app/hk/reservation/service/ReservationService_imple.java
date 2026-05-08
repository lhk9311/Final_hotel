package com.spring.app.hk.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.common.AES256;
import com.spring.app.hk.reservation.mail.ReservationMailService;
import com.spring.app.hk.reservation.model.ReservationDAO;
import com.spring.app.hk.room.service.RoomStockService;
import com.spring.app.jh.security.domain.CustomUserDetails;
import com.spring.app.jh.security.domain.Session_GuestDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService_imple implements ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomStockService roomStockService;
    private final AES256 aes256;
    
    private final ReservationMailService reservationMailService; // 추가 : 메일
    private final SmsService smsService; // 추가 : sms

    // 예약 페이지 내 객실 기본 정보 조회
 	@Override
 	public Map<String, Object> getRoomInfo(int room_type_id) {

 	    Map<String, Object> roomInfo = reservationDAO.getRoomInfo(room_type_id);

 	    if (roomInfo == null) {
 	        throw new IllegalArgumentException("해당 객실이 존재하지 않습니다.");
 	    }

 	    return roomInfo;
 	}
    
	// 소셜로그인용
	@Override
	public Map<String, Object> findMemberByEmail(String emailFromOauth) {
		return reservationDAO.findMemberByEmail(emailFromOauth);
	}

 	
    // 결제 성공 후 db 저장하기
    @Override
    public String saveReservation(Map<String, String> map, HttpSession session) {

        System.out.println("========== ReservationService.saveReservation() 시작 ==========");
        System.out.println("▶ 전달받은 map : " + map);

        Map<String, Object> paraMap = new HashMap<>(map);

        Integer memberNo = null;
        String email = null;
        String name = null;

        // 1️. Spring Security 로그인 회원
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("▶ authentication : " + authentication);

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            memberNo = userDetails.getMemberDto().getMemberNo();
            email = userDetails.getMemberDto().getEmail();
            name = userDetails.getMemberDto().getName();

            System.out.println("▶ [회원 로그인] memberNo : " + memberNo);
            System.out.println("▶ [회원 로그인] email    : " + email);
            System.out.println("▶ [회원 로그인] name     : " + name);
        }
        
     // ⭐ 소셜 로그인 처리 추가
        else if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauthUser) {

            System.out.println("OAuth 로그인 감지");

            String emailFromOauth = null;

            // 네이버
            Map<String, Object> response = (Map<String, Object>) oauthUser.getAttributes().get("response");
            if (response != null) {
                emailFromOauth = (String) response.get("email");
            }

            // 카카오
            if (emailFromOauth == null) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauthUser.getAttributes().get("kakao_account");
                if (kakaoAccount != null) {
                    emailFromOauth = (String) kakaoAccount.get("email");
                }
            }

            
            if (emailFromOauth != null) {

                emailFromOauth = emailFromOauth.trim();

                Map<String, Object> member = null;

                try {
                    // 1️⃣ 암호화해서 조회 시도
                    String encryptedEmail = aes256.encrypt(emailFromOauth);
                    member = reservationDAO.findMemberByEmail(encryptedEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2️⃣ 그래도 없으면 → 그냥 생성
                if (member == null) {

                    System.out.println("회원 없음 → 자동 생성");

                    reservationDAO.insertSocialMember(emailFromOauth); // 평문 저장

                    member = reservationDAO.findMemberByEmail(emailFromOauth);
                }

                // 3️⃣ 값 세팅
                if (member != null) {
                    memberNo = ((Number) member.get("MEMBER_NO")).intValue();
                    email = (String) member.get("EMAIL");
                    name = (String) member.get("NAME");
                }
            }
        }

        // 2️. 비회원 로그인
        if (memberNo == null) {

            Session_GuestDTO guest = (Session_GuestDTO) session.getAttribute("guestSession");
            System.out.println("▶ guestSession : " + guest);

            if (guest != null) {
                memberNo = guest.getMemberNo();
                name = guest.getGuestName();

                System.out.println("▶ [비회원 로그인] memberNo : " + memberNo);
                System.out.println("▶ [비회원 로그인] name     : " + name);
            }
        }

        // 로그인 안된 경우 방지
        if (memberNo == null) {
            System.out.println("❌ memberNo 가 null 입니다. 로그인/비회원 세션이 없는 상태입니다.");
            throw new RuntimeException("로그인 또는 비회원 로그인이 필요합니다.");
        }

        paraMap.put("member_no", memberNo);
        System.out.println("▶ paraMap.member_no : " + paraMap.get("member_no"));

        // 인원수 처리
        String guestCount = map.getOrDefault("guest_count", "1");
        paraMap.put("guest_count", guestCount);
        System.out.println("▶ guest_count : " + guestCount);

        // imp_uid null 방지
        if (paraMap.get("imp_uid") == null) {
            paraMap.put("imp_uid", "");
        }
        System.out.println("▶ imp_uid : " + paraMap.get("imp_uid"));

        // 가격 결정
        if (map.containsKey("applied_price") && !map.get("applied_price").isEmpty()) {
            paraMap.put("total_price", map.get("applied_price"));
        } else {
            paraMap.put("total_price", map.getOrDefault("room_price", "100"));
        }
        System.out.println("▶ total_price : " + paraMap.get("total_price"));

        // 날짜 처리
        System.out.println("▶ room_type_id 원본값 : " + map.get("room_type_id"));
        System.out.println("▶ check_in   원본값 : " + map.get("check_in"));
        System.out.println("▶ check_out  원본값 : " + map.get("check_out"));

        int roomId = Integer.parseInt(map.get("room_type_id"));
        LocalDate checkIn = LocalDate.parse(map.get("check_in"));
        LocalDate checkOut = LocalDate.parse(map.get("check_out"));

        System.out.println("▶ roomId   : " + roomId);
        System.out.println("▶ checkIn  : " + checkIn);
        System.out.println("▶ checkOut : " + checkOut);

        LocalDateTime cancelDeadline = checkIn.atStartOfDay().minusDays(1);
        paraMap.put("cancel_deadline", cancelDeadline);
        paraMap.put("refund_amount", 0);

        System.out.println("▶ cancel_deadline : " + cancelDeadline);
        System.out.println("▶ refund_amount   : " + paraMap.get("refund_amount"));

        // 재고 차감
        try {
            System.out.println("========== 재고 차감 시작 ==========");
            roomStockService.decreaseStockByDateRange(roomId, checkIn, checkOut);
            System.out.println("✅ 재고 차감 완료");
        } catch (Exception e) {
            System.out.println("❌ 재고 차감 중 예외 발생");
            e.printStackTrace();
            throw new RuntimeException("객실 재고 차감 중 오류가 발생했습니다. " + e.getMessage(), e);
        }

        // PAYMENT insert
        try {
            System.out.println("========== PAYMENT insert 시작 ==========");
            reservationDAO.insertPayment(paraMap);
            System.out.println("✅ PAYMENT insert 완료");
            System.out.println("▶ payment_id(insert 후 paraMap) : " + paraMap.get("payment_id"));
           
        } catch (Exception e) {
            System.out.println("❌ PAYMENT insert 중 예외 발생");
            e.printStackTrace();
            throw new RuntimeException("결제 정보 저장 중 오류가 발생했습니다. " + e.getMessage(), e);
        }

        // RESERVATION insert
        try {
            System.out.println("========== RESERVATION insert 시작 ==========");
            reservationDAO.insertReservation(paraMap);
            System.out.println("✅ RESERVATION insert 완료");
            System.out.println("▶ reservation_id(insert 후 paraMap) : " + paraMap.get("reservation_id"));
        } catch (Exception e) {
            System.out.println("❌ RESERVATION insert 중 예외 발생");
            e.printStackTrace();
            throw new RuntimeException("예약 정보 저장 중 오류가 발생했습니다. " + e.getMessage(), e);
        }

        Long reservationId = (Long) paraMap.get("reservation_id");

        if (reservationId == null) {
            System.out.println("❌ reservation_id 가 null 입니다.");
            throw new RuntimeException("예약번호 생성에 실패했습니다.");
        }

        // 예약 코드 생성
        String reservationCode = "R"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%08d", reservationId);

        // ======================= SMS 발송 =======================
        try {
            System.out.println("========== SMS 발송 시작 ==========");

            String phone = null;

            // 회원
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String encryptedPhone = userDetails.getMemberDto().getMobile();

                if (encryptedPhone != null) {
                    phone = aes256.decrypt(encryptedPhone);
                }
            }

            // 비회원
            if (phone == null) {
                Session_GuestDTO guest = (Session_GuestDTO) session.getAttribute("guestSession");
                if (guest != null) {
                    phone = guest.getGuestPhone();
                }
            }

            if (phone != null) {
                phone = phone.replace("-", "");

                String msg = "[호텔 예약 완료]\n"
                           + name + "님\n"
                           + "예약번호: " + reservationCode + "\n"
                           + map.get("hotel_name") + "\n"
                           + map.get("check_in") + " ~ " + map.get("check_out");

                smsService.sendReservationSms(phone, msg);

                System.out.println("✅ SMS 발송 완료");
            }

        } catch (Exception e) {
            System.out.println("❌ SMS 발송 실패");
            e.printStackTrace();
        }
        
        System.out.println("▶ reservationCode : " + reservationCode);

        // 회원일 경우만 이메일 전송
        if (email != null) {
            try {
                System.out.println("========== 예약 메일 전송 시작 ==========");
                reservationMailService.sendReservationMail(
                        email,
                        name,
                        reservationCode,
                        map.get("hotel_name"),
                        map.get("room_name"),
                        map.get("check_in"),
                        map.get("check_out"),
                        String.valueOf(paraMap.get("total_price"))
                );
                System.out.println("✅ 예약 메일 전송 완료");
            } catch (Exception e) {
                System.out.println("❌ 예약 메일 전송 중 예외 발생");
                e.printStackTrace();
            }
        }

        System.out.println("========== ReservationService.saveReservation() 종료 ==========");
        return reservationCode;
    }

    
    // 예약 완료 페이지
	@Override
	public Map<String, Object> getReservationByCode(String code) {
		return reservationDAO.findByReservationCode(code);
	}
	

	// 마이페이지 예약 목록 조회
	@Override
	public List<Map<String, Object>> selectMyReservationList(int memberNo) {
		return reservationDAO.selectMyReservationList(memberNo);
	}


	// 예약 취소
	@Override
	public int cancelReservation(long reservationId) {
	    return reservationDAO.cancelReservation(reservationId);
	}


	// 비회원 예약 조회
	@Override
	public List<Map<String,Object>> findGuestReservation(String name, String phone){

	    Map<String,Object> paraMap = new HashMap<>();
	    paraMap.put("name", name);
	    paraMap.put("phone", phone);

	    return reservationDAO.findGuestReservation(paraMap);
	}


	// 비회원 예약 취소
	@Override
	public int cancelGuestReservation(String reservationCode) {
		return reservationDAO.cancelGuestReservation(reservationCode);
	}
	
	
	// 스프링 스케줄러로 예약 메일 보내기
	public void sendCheckinReminderMail() {

	    List<Map<String, Object>> list = reservationDAO.selectTomorrowCheckinForMail();

	    for(Map<String, Object> row : list) {

	    	String encryptedEmail = (String) row.get("EMAIL");
	    	String email = null;

	    	try {
	    	    email = aes256.decrypt(encryptedEmail);  // 복호화
	    	} catch (Exception e) {
	    	    System.out.println("❌ 이메일 복호화 실패: " + encryptedEmail);
	    	    continue;
	    	}

	    	// 안전 필터
	    	if(email == null || !email.contains("@")) {
	    	    System.out.println("❌ 잘못된 이메일: " + email);
	    	    continue;
	    	}
	        
	        String name = (String) row.get("NAME");

	        String reservationCode = (String) row.get("RESERVATION_CODE");
	        String hotelName = (String) row.get("HOTEL_NAME");
	        String roomName = (String) row.get("ROOM_NAME");

	        String checkIn = String.valueOf(row.get("CHECKIN_DATE"));
	        String checkOut = String.valueOf(row.get("CHECKOUT_DATE"));
	        String totalPrice = String.valueOf(row.get("TOTAL_PRICE"));

	        try {
	        	reservationMailService.sendReminderMail(
	        		    email,
	        		    name,
	        		    reservationCode,
	        		    hotelName,
	        		    roomName,
	        		    checkIn,
	        		    checkOut
	        		);

	            System.out.println("✅ 체크인 하루 전 메일 발송 완료 : " + reservationCode);

	        } catch (Exception e) {
	            System.out.println("❌ 메일 발송 실패 : " + reservationCode);
	            e.printStackTrace();
	        }
	    }
	}


}
