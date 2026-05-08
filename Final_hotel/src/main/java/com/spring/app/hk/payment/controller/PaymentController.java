package com.spring.app.hk.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.spring.app.hk.reservation.service.ReservationService;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final ReservationService reservationService;

	// 포트원 REST API 키 (application.yml에서 읽음)
	@Value("${iamport.api-key}")
	private String apiKey;

	@Value("${iamport.api-secret}")
	private String apiSecret;

	// 예약
	@PostMapping("/verify")
	public ResponseEntity<String> verify(@RequestBody Map<String, String> map,
	                                     HttpSession session) {

	    System.out.println("==== /payment/verify 호출됨 ====");
	    System.out.println("==== 전달받은 map = " + map);

	    String impUid = map.get("imp_uid");
	    System.out.println("==== 결제 완료 imp_uid = " + impUid);

	    try {

	        // ==========================================
	        // 1. 포트원 결제 검증
	        // ==========================================
	        try {
	            RestTemplate restTemplate = new RestTemplate();

	            String tokenUrl = "https://api.iamport.kr/users/getToken";

	            Map<String, String> tokenRequest = new HashMap<>();
	            tokenRequest.put("imp_key", apiKey);
	            tokenRequest.put("imp_secret", apiSecret);

	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);

	            HttpEntity<Map<String, String>> tokenEntity =
	                    new HttpEntity<>(tokenRequest, headers);

	            ResponseEntity<Map> tokenResponse =
	                    restTemplate.postForEntity(tokenUrl, tokenEntity, Map.class);

	          /*
	            String accessToken =
	                    (String) ((Map) tokenResponse.getBody().get("response"))
	                            .get("access_token");
	           */
	            // 소나큐브 (npe 가능성 있어서 바꿈!)
	            Map responseBody = tokenResponse.getBody();
	            if (responseBody == null) {
	                throw new Exception("토큰 응답이 null입니다.");
	            }
	            String accessToken =
	                    (String) ((Map) responseBody.get("response"))
	                            .get("access_token");            
	            
	            String paymentUrl = "https://api.iamport.kr/payments/" + impUid;

	            // 
	            HttpHeaders paymentHeaders = new HttpHeaders();
	            paymentHeaders.set("Authorization", accessToken);

	            HttpEntity<String> paymentEntity =
	                    new HttpEntity<>(paymentHeaders);

	            restTemplate.exchange(paymentUrl, HttpMethod.GET, paymentEntity, Map.class);

	        } catch (Exception e) {
	            System.out.println("==== 외부 결제 API 검증 실패 로그 처리 ====");
	            e.printStackTrace();
	        }

	        // ==========================================
	        // 2. 예약 저장
	        // ==========================================
	        String reservationCode = reservationService.saveReservation(map, session);

	        System.out.println("==== 예약 저장 성공 reservationCode = " + reservationCode);

	        return ResponseEntity.ok(reservationCode);

	    } catch (Exception e) {
	        e.printStackTrace();

	        String errorMsg = "예약 저장 실패: " + e.getMessage();
	        System.out.println("==== " + errorMsg);

	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
	    }
	}
}