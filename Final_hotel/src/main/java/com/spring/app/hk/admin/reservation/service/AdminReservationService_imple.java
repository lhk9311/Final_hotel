package com.spring.app.hk.admin.reservation.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.spring.app.hk.admin.reservation.model.AdminReservationDAO;

@Service
@RequiredArgsConstructor
public class AdminReservationService_imple implements AdminReservationService {

    private final AdminReservationDAO reservationDAO;

    @Override
    public List<Map<String,Object>> getTodayCheckinList() {
        return reservationDAO.getTodayCheckinList();
    }

    @Override
    public List<Map<String,Object>> getTodayCheckoutList() {
        return reservationDAO.getTodayCheckoutList();
    }

    @Override
    public void checkinReservation(int reservationId) {
        reservationDAO.checkinReservation(reservationId);
    }

    @Override
    public void checkoutReservation(int reservationId) {
        reservationDAO.checkoutReservation(reservationId);
    }

	@Override
	public List<Map<String, Object>> getStayList() {
		return reservationDAO.getStayList();
	}

	@Override
	public List<Map<String, Object>> getCheckoutCompleteList() {
		return reservationDAO.getCheckoutCompleteList();
	}

	@Override
	public List<Map<String, Object>> selectAdminReservationList(Map<String, Object> param) {
		return reservationDAO.selectAdminReservationList(param);
	}

	@Override
	public List<Map<String, Object>> getOverdueList() {
	    return reservationDAO.getOverdueList();
	}

	@Override
	public int getTodayCheckinTotalCount() {
	    return reservationDAO.getTodayCheckinTotalCount();
	}

	@Override
	public int getTodayCheckinDoneCount() {
	    return reservationDAO.getTodayCheckinDoneCount();
	}

	@Override
	public List<Map<String, Object>> selectHotelList() {
		return reservationDAO.selectHotelList();
	}

	@Override
	public List<Map<String, Object>> getNoShowList() {
	    return reservationDAO.getNoShowList();
	}

	@Override
	public void noShowReservation(int reservationId) {
	    reservationDAO.updateNoShow(reservationId);
	}
	
	// 추가(페이징)
	@Override
	public List<Map<String, Object>> selectAdminReservationListForExcel(Map<String, Object> param) {
	    return reservationDAO.selectAdminReservationListForExcel(param);
	}
	
}