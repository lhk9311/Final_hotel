package com.spring.app.hk.admin.room.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface AdminRoomService {

	// ======== 지점관리자 ========
	// 지점 객실 목록 조회
	List<RoomTypeDTO> getRoomListByManager(Integer adminNo);
	
	// 운영중 객실 수정
	void modifyApprovedRoom(Map<String, String> map, MultipartFile roomImage);
	
	// 객실 등록 신청시 객실 + 이미지 저장
	void saveRoom(Map<String, Object> paraMap);
	
	// 객실 반려 후 수정 (이미지 교체 처리)
	void updateRoom(Map<String, String> map, MultipartFile roomImage);
	
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
	List<Map<String,Object>> getApprovalHistoryList();

	// 호텔 필터 추가
	Object selectHotelList();

	// 엑셀 업로드
	void insertRoomFromExcel(Map<String, Object> room);

	

	

	
	
	

}