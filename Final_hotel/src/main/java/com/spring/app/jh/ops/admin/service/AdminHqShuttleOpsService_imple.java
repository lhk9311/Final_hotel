package com.spring.app.jh.ops.admin.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.jh.ops.admin.common.domain.HotelSimpleDTO;
import com.spring.app.jh.ops.admin.common.domain.ShuttleAdminTimetableDTO;
import com.spring.app.jh.ops.admin.common.domain.ShuttleBlockDTO;
import com.spring.app.jh.ops.admin.common.domain.ShuttlePlaceDTO;
import com.spring.app.jh.ops.admin.common.domain.ShuttleRouteDTO;
import com.spring.app.jh.ops.admin.model.AdminHqShuttleOpsDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminHqShuttleOpsService_imple implements AdminHqShuttleOpsService {

    private final AdminHqShuttleOpsDAO shuttleDao;

    @Override
    public List<HotelSimpleDTO> getHotelList() {
        return shuttleDao.selectHotelList();
    }

    @Override
    public List<ShuttlePlaceDTO> getPlaceList() {
        return shuttleDao.selectPlaceList();
    }

    @Override
    public List<ShuttleRouteDTO> getRouteList(int hotelId) {
        return shuttleDao.selectRouteList(hotelId);
    }

    @Override
    public List<ShuttleAdminTimetableDTO> getTimetableList(int hotelId) {
        return shuttleDao.selectTimetableList(hotelId);
    }

    @Override
    public List<ShuttleBlockDTO> getBlockList(int hotelId) {
        return shuttleDao.selectBlockList(hotelId);
    }

    @Override
    @Transactional
    public int addRoute(int hotelId,
                        String routeType,
                        String startPlaceCode,
                        String endPlaceCode,
                        String routeName) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException("호텔 번호가 올바르지 않습니다.");
        }

        if (routeType == null || (!"TO_HOTEL".equals(routeType) && !"FROM_HOTEL".equals(routeType))) {
            throw new IllegalArgumentException("routeType 값이 올바르지 않습니다.");
        }

        if (startPlaceCode == null || endPlaceCode == null) {
            throw new IllegalArgumentException("출발지/도착지는 필수입니다.");
        }

        if (startPlaceCode.equals(endPlaceCode)) {
            throw new IllegalArgumentException("출발지와 도착지는 같을 수 없습니다.");
        }

        // 화면에서 "선택 호텔" 은 HOTEL_{hotelId} 코드로 넘어오도록 맞춘 상태
        String hotelPlaceCode = "HOTEL_" + hotelId;

        // 항상 false (소나큐브
        /*
        if ("TO_HOTEL".equals(routeType)) {

            // TO_HOTEL : 외부 장소 -> 선택 호텔
            if (!hotelPlaceCode.equals(endPlaceCode)) {
                throw new IllegalArgumentException("TO_HOTEL 노선의 도착지는 선택 호텔이어야 합니다.");
            }

            if (hotelPlaceCode.equals(startPlaceCode)) {
                throw new IllegalArgumentException("TO_HOTEL 노선의 출발지는 외부 장소여야 합니다.");
            }
        }

        if ("FROM_HOTEL".equals(routeType)) {

            // FROM_HOTEL : 선택 호텔 -> 외부 장소
            if (!hotelPlaceCode.equals(startPlaceCode)) {
                throw new IllegalArgumentException("FROM_HOTEL 노선의 출발지는 선택 호텔이어야 합니다.");
            }

            if (hotelPlaceCode.equals(endPlaceCode)) {
                throw new IllegalArgumentException("FROM_HOTEL 노선의 도착지는 외부 장소여야 합니다.");
            }
        }       
        */
        if ("TO_HOTEL".equals(routeType)) {
            if (!hotelPlaceCode.equals(endPlaceCode)) {
                throw new IllegalArgumentException("TO_HOTEL 노선의 도착지는 선택 호텔이어야 합니다.");
            }
            if (startPlaceCode.equals(hotelPlaceCode)) {
                throw new IllegalArgumentException("TO_HOTEL 노선의 출발지는 외부 장소여야 합니다.");
            }
        }

        if ("FROM_HOTEL".equals(routeType)) {
            if (!hotelPlaceCode.equals(startPlaceCode)) {
                throw new IllegalArgumentException("FROM_HOTEL 노선의 출발지는 선택 호텔이어야 합니다.");
            }
            if (endPlaceCode.equals(hotelPlaceCode)) {
                throw new IllegalArgumentException("FROM_HOTEL 노선의 도착지는 외부 장소여야 합니다.");
            }
        }

        if (routeName == null || routeName.trim().isEmpty()) {
            throw new IllegalArgumentException("노선명은 필수입니다.");
        }

        return shuttleDao.insertRoute(hotelId, routeType, startPlaceCode, endPlaceCode, routeName);
    }

    @Override
    @Transactional
    public int deactivateRoute(int hotelId, long routeId) {
        shuttleDao.deactivateTimetableByRoute(hotelId, routeId);
        return shuttleDao.deactivateRoute(hotelId, routeId);
    }

    @Override
    @Transactional
    public int addBlock(int adminNo,
                        int hotelId,
                        long routeId,
                        Long timetableId,
                        LocalDate startDate,
                        LocalDate endDate,
                        String reason) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException("호텔 번호가 올바르지 않습니다.");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("차단 시작일과 종료일은 필수입니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("차단 종료일은 시작일보다 빠를 수 없습니다.");
        }

        return shuttleDao.insertBlock(adminNo, hotelId, routeId, timetableId, startDate, endDate, reason);
    }

    @Override
    @Transactional
    public int deactivateBlock(int hotelId, long blockId) {
        return shuttleDao.deactivateBlock(hotelId, blockId);
    }

    @Override
    @Transactional
    public int purgeOldShuttleData(int hotelId) {

        if (hotelId <= 0) {
            throw new IllegalArgumentException("호텔 번호가 올바르지 않습니다.");
        }

        LocalDate cutoffDate = LocalDate.now().minusMonths(1);

        int cnt1 = shuttleDao.deleteOldBooking(hotelId, cutoffDate);
        int cnt2 = shuttleDao.deleteOldSlotStock(hotelId, cutoffDate);

        return cnt1 + cnt2;
    }
}