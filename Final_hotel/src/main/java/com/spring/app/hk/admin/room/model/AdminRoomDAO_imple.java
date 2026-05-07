package com.spring.app.hk.admin.room.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.app.hk.room.domain.RoomTypeDTO;

@Repository
public class AdminRoomDAO_imple implements AdminRoomDAO {

	@Autowired
	private SqlSessionTemplate sqlsession;

	// 지점 객실 목록 조회
	@Override
	public List<RoomTypeDTO> getRoomListByManager(Integer adminNo) {
		return sqlsession.selectList("adminRoom.getRoomListByManager", adminNo);
	}

	// 운영중 객실 수정
	@Override
	public void modifyApprovedRoom(Map<String, String> map) {
		sqlsession.update("adminRoom.modifyApprovedRoom", map);
	}

	// 객실 등록 신청시
	// 1. 객실 등록
	@Override
	public void insertRoom(Map<String, Object> paraMap) {
		sqlsession.insert("adminRoom.insertRoom", paraMap);

	}

	// 2. 이미지 등록
	@Override
	public void insertRoomImage(Map<String, Object> paraMap) {
		sqlsession.insert("adminRoom.insertRoomImage", paraMap);

	}

	// 3. 승인시 room stock 등록
	@Override
	public void insertRoomStock365(int roomTypeId) {
		sqlsession.insert("adminRoom.insertRoomStock365", roomTypeId);

	}

	// 반려후 재상신시
	// 1. 객실 정보 수정
	@Override
	public void updateRoom(Map<String, String> map) {
		sqlsession.update("adminRoom.updateRoom", map);
	}

	// 2. 객실 이미지 수정
	@Override
	public void updateRoomImage(Map<String, Object> imageMap) {
		sqlsession.update("adminRoom.updateRoomImage", imageMap);
	}

	// 반려 후 재상신
	@Override
	public void resubmitRoom(int roomTypeId) {
		sqlsession.update("adminRoom.resubmitRoom", roomTypeId);

	}

	// 객실 비활성화
	@Override
	public void deactivateRoom(int roomTypeId) {
		sqlsession.update("adminRoom.deactivateRoom", roomTypeId);
	}

	// 비활성 객실 조회
	@Override
	public List<RoomTypeDTO> getInactiveRoomListByManager(Integer adminNo) {
		return sqlsession.selectList("adminRoom.getInactiveRoomListByManager", adminNo);
	}
	
	// 객실 복구(활성화)
	@Override
	public void restoreRoom(int roomTypeId) {
	    sqlsession.update("adminRoom.restoreRoom", roomTypeId);
	}

	// 총관관리자
	// 승인 대기 객실 목록 조회 (안씀)
	@Override
	public List<RoomTypeDTO> getPendingRoomList() {
		return sqlsession.selectList("adminRoom.getPendingRoomList");
	}

	// 전체 객실 조회
	@Override
	public List<RoomTypeDTO> getRoomApprovalList() {
		return sqlsession.selectList("adminRoom.getRoomApprovalList");
	}

	// 객실 승인
	@Override
	public void approveRoom(int roomTypeId, int adminId) {

		Map<String, Object> param = new HashMap<>();
		param.put("roomTypeId", roomTypeId);

		sqlsession.update("adminRoom.approveRoom", param);

		param.put("room_type_id", roomTypeId);
		param.put("status", "APPROVED");
		param.put("reason", null);
		param.put("admin_id", adminId);

		sqlsession.insert("adminRoom.insertApprovalHistory", param);
	}

	// 객실 반려
	@Override
	public void rejectRoom(int roomTypeId, int adminId, String reason) {

		Map<String, Object> param = new HashMap<>();
		param.put("roomTypeId", roomTypeId);

		sqlsession.update("adminRoom.rejectRoom", param);

		param.put("room_type_id", roomTypeId);
		param.put("status", "REJECTED");
		param.put("reason", reason);
		param.put("admin_id", adminId);

		sqlsession.insert("adminRoom.insertApprovalHistory", param);
	}

	// 객실 승인 히스토리 조회
	@Override
	public List<Map<String, Object>> getRoomApprovalHistory(int roomTypeId) {
		return sqlsession.selectList("adminRoom.getRoomApprovalHistory", roomTypeId);
	}

	// 객실 전체 승인 히스토리 조회용
	@Override
	public List<Map<String, Object>> getApprovalHistoryList() {
		return sqlsession.selectList("adminRoom.getApprovalHistoryList");

	}

	// 객실 전체 승인 히스토리 조회용
	@Override
	public List<Map<String, Object>> getBranchApprovalHistoryList(Integer adminNo) {
		return sqlsession.selectList("adminRoom.getBranchApprovalHistoryList", adminNo);
	}
	
	// 호텔 필터용
	@Override
	public List<Map<String, Object>> selectHotelList() {
	    return sqlsession.selectList("adminRoom.selectHotelList");
	}
	
	// 추가
	@Override
	public void insertRoomFromExcel(Map<String, Object> paraMap) {
	    sqlsession.insert("adminRoom.insertRoomFromExcel", paraMap);
	}

}