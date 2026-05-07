package com.spring.app.hk.admin.room.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.common.FileManager;
import com.spring.app.hk.admin.room.model.AdminRoomDAO;
import com.spring.app.hk.room.domain.RoomTypeDTO;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRoomService_imple implements AdminRoomService {

    private final AdminRoomDAO adminRoomDAO;

    private final FileManager fileManager;

    @Value("${file.images-dir}")
    private String imagesDir;
    
    // ======== 지점관리자 ========
    // 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getRoomListByManager(Integer adminNo) {

        List<RoomTypeDTO> roomList = adminRoomDAO.getRoomListByManager(adminNo);

        return roomList;
    }
    
    
    // 운영중 객실 수정
    @Override
    public void modifyApprovedRoom(Map<String, String> map, MultipartFile roomImage) {

        adminRoomDAO.modifyApprovedRoom(map);

        int roomTypeId = Integer.parseInt(map.get("roomTypeId"));

        if (roomImage != null && !roomImage.isEmpty()) {
            try {
                String roomPath = imagesDir + File.separator + "room";

                String savedName = fileManager.doFileUpload(
                        roomImage.getBytes(),
                        roomImage.getOriginalFilename(),
                        roomPath
                );

                Map<String, Object> imageMap = new HashMap<>();
                imageMap.put("roomTypeId", roomTypeId);
                imageMap.put("image_url", "/file_images/room/" + savedName);

                adminRoomDAO.updateRoomImage(imageMap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    // 객실 등록 신청시 객실 + 이미지 저장
    @Override
    public void saveRoom(Map<String, Object> paraMap) {

        // 1️. 객실 등록
        adminRoomDAO.insertRoom(paraMap);

        // 생성된 room_type_id 가져오기
        int roomTypeId = (int) paraMap.get("room_type_id");

        // 2️. 이미지 저장
        if(paraMap.get("image_url") != null){

            paraMap.put("fk_room_type_id", roomTypeId);
            paraMap.put("is_main", "Y");
            paraMap.put("sort_order", 1);

            adminRoomDAO.insertRoomImage(paraMap);
        }

        System.out.println("객실 + 이미지 저장 완료");
    }

    
    // 객실 반려 후 수정 (이미지 교체 처리)
    @Override
    public void updateRoom(Map<String, String> map, MultipartFile roomImage) {

        adminRoomDAO.updateRoom(map);

        int roomTypeId = Integer.parseInt(map.get("roomTypeId"));

        if (roomImage != null && !roomImage.isEmpty()) {
            try {
                String roomPath = imagesDir + File.separator + "room";

                String savedName = fileManager.doFileUpload(
                        roomImage.getBytes(),
                        roomImage.getOriginalFilename(),
                        roomPath
                );

                Map<String, Object> imageMap = new HashMap<>();
                imageMap.put("roomTypeId", roomTypeId);
                imageMap.put("image_url", "/file_images/room/" + savedName);

                adminRoomDAO.updateRoomImage(imageMap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 	
 	
 	// 반려 후 재상신
 	@Override
 	public void resubmitRoom(int roomTypeId) {
 		adminRoomDAO.resubmitRoom(roomTypeId);
 		
 	}
    
 	
 	// 승인 히스토리 조회
	@Override
	public List<Map<String, Object>> getBranchApprovalHistoryList(Integer adminNo) {
		return adminRoomDAO.getBranchApprovalHistoryList(adminNo);
	}

    
	// 객실 비활성화
	@Override
	public void deactivateRoom(int roomTypeId) {
		adminRoomDAO.deactivateRoom(roomTypeId);		
	}
	
	
	// 비활성 객실 조회
	@Override
	public List<RoomTypeDTO> getInactiveRoomListByManager(Integer adminNo) {
		return adminRoomDAO.getInactiveRoomListByManager(adminNo);
	}
    
	
	// 객실 복구 (활성화)
	@Override
	public void restoreRoom(int roomTypeId) {
		adminRoomDAO.restoreRoom(roomTypeId);
	}
	
	
    // ======== 총괄관리자 ========
    // 승인 대기 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getPendingRoomList() {
        return adminRoomDAO.getPendingRoomList();
    }

    // 객실 승인
    @Override
    public void approveRoom(int roomTypeId, int adminId) {
        
    	// 1. 객실 승인
    	adminRoomDAO.approveRoom(roomTypeId, adminId);
    	
    	// 2 ROOM_STOCK 생성 (365일)
        adminRoomDAO.insertRoomStock365(roomTypeId);
    }

    // 객실 반려
    @Override
    public void rejectRoom(int roomTypeId, int adminId, String reason) {
        adminRoomDAO.rejectRoom(roomTypeId, adminId, reason);
    }


    // 전체 객실 목록 조회
	@Override
	public List<RoomTypeDTO> getRoomApprovalList() {
		 return adminRoomDAO.getRoomApprovalList();
	}


	// 객실 승인 히스토리 조회
	@Override
	public List<Map<String, Object>> getRoomApprovalHistory(int roomTypeId) {
		 return adminRoomDAO.getRoomApprovalHistory(roomTypeId);
	}

	
	// 객실 전체 승인 히스토리 조회용
	@Override
	public List<Map<String, Object>> getApprovalHistoryList() {
	    return adminRoomDAO.getApprovalHistoryList();

	}

	// 호텔 필터용
	@Override
	public List<Map<String, Object>> selectHotelList() {
	    return adminRoomDAO.selectHotelList();
	}


	// 엑셀 업로드
	@Override
	public void insertRoomFromExcel(Map<String, Object> room) {
	    room.put("approve_status", "PENDING");
	    room.put("is_active", "Y");
	    adminRoomDAO.insertRoomFromExcel(room);
	}

	
}