package com.spring.app.hk.admin.room.model;

import java.util.List;
import java.util.Map;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface AdminRoomDAO {

	// ======== 지점관리자 ========
	// 지점 객실 목록 조회
	List<RoomTypeDTO> getRoomListByManager(Integer adminNo);
	
	// 운영중 객실 수정
	void modifyApprovedRoom(Map<String, String> map);
	
	// 객실 등록 신청시
	// 1. 객실 등록
	void insertRoom(Map<String, Object> paraMap);

	// 2. 이미지 저장
	void insertRoomImage(Map<String, Object> paraMap);
	
	// 3. 승인시 room stock 등록
	void insertRoomStock365(int roomTypeId);
	
	// 이미지 교체 후 수정
	void updateRoom(Map<String, String> map);
	void updateRoomImage(Map<String, Object> imageMap);
	
	// 반려 후 재상신
	void resubmitRoom(int roomTypeId);
	
	// 승인 히스토리 조회
	List<Map<String, Object>> getBranchApprovalHistoryList(Integer adminNo);
	
	// 객실 비활성화
	void deactivateRoom(int roomTypeId);
	
	// 비활성 객실 조회
	List<RoomTypeDTO> getInactiveRoomListByManager(Integer adminNo);
	
	// 객실 복구 (활성화)
	void restoreRoom(int roomTypeId);
	
	// ======== 총괄관리자 ========
	// 승인 대기 객실 목록 조회
	List<RoomTypeDTO> getPendingRoomList();

	// 객실 승인
	void approveRoom(int roomTypeId, int adminId);

	// 객실 반려
	void rejectRoom(int roomTypeId, int adminId, String reason);

	// 전체 객실 목록 조회
	List<RoomTypeDTO> getRoomApprovalList();

	// 객실 승인 히스토리 조회
	List<Map<String, Object>> getRoomApprovalHistory(int roomTypeId);

	// 객실 전체 승인 히스토리 조회용
	List<Map<String, Object>> getApprovalHistoryList();

	// 호텔 필터용
	List<Map<String, Object>> selectHotelList();
	
	// 추가
	void insertRoomFromExcel(Map<String, Object> paraMap);

	

  
	
	
}