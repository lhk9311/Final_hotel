package com.spring.app.hk.admin.reservation.model;

import java.util.List;
import java.util.Map;

public interface AdminReservationDAO {

	// ----- 지점관리자 ----- 
	// 오늘 체크인 리스트
    List<Map<String,Object>> getTodayCheckinList();

    // 오늘 체크아웃 리스트
    List<Map<String,Object>> getTodayCheckoutList();

    // 체크인 처리
    void checkinReservation(int reservationId);

    // 체크아웃 처리
    void checkoutReservation(int reservationId);

    // 투숙중 리스트
	List<Map<String, Object>> getStayList();

	// 체크아웃 완료 목록 조회
	List<Map<String, Object>> getCheckoutCompleteList();
	
	List<Map<String, Object>> getOverdueList();
	
	List<Map<String, Object>> getNoShowList();

	void updateNoShow(int reservationId);

	Map<String, Object> findReservation(int reservationId);
	
	// ----- 총괄관리자 ----- 
	// 전체 객실 예약 리스트 조회
	List<Map<String, Object>> selectAdminReservationList(Map<String, Object> param);

	int getTodayCheckinTotalCount();

	int getTodayCheckinDoneCount();

	// 호텔 조회 필터 추가
	List<Map<String, Object>> selectHotelList();
	
	// 추가(페이징)
	List<Map<String, Object>> selectAdminReservationListForExcel(Map<String, Object> param);

	

	

}