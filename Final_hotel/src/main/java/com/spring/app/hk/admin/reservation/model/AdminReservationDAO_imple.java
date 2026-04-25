package com.spring.app.hk.admin.reservation.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AdminReservationDAO_imple implements AdminReservationDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // 오늘 체크인 리스트
    @Override
    public List<Map<String,Object>> getTodayCheckinList() {
        return sqlsession.selectList("adminReservation.getTodayCheckinList");
    }

    // 오늘 체크아웃 리스트
    @Override
    public List<Map<String,Object>> getTodayCheckoutList() {
        return sqlsession.selectList("adminReservation.getTodayCheckoutList");
    }

    // 체크인 처리
    @Override
    public void checkinReservation(int reservationId) {
        sqlsession.update("adminReservation.checkinReservation", reservationId);
    }

    // 체크아웃 처리
    @Override
    public void checkoutReservation(int reservationId) {
        sqlsession.update("adminReservation.checkoutReservation", reservationId);
    }

    
    // 투숙중 리스트
	@Override
	public List<Map<String, Object>> getStayList() {
		return sqlsession.selectList("adminReservation.getStayList");
	}

	
	@Override
	public List<Map<String, Object>> getOverdueList() {
	    return sqlsession.selectList("adminReservation.getOverdueList");
	}
	
	
	// 체크아웃 완료 목록 조회
	@Override
	public List<Map<String, Object>> getCheckoutCompleteList() {
		return sqlsession.selectList("adminReservation.getCheckoutCompleteList");
	}

	
	@Override
	public int getTodayCheckinTotalCount() {
	    return sqlsession.selectOne("adminReservation.getTodayCheckinTotalCount");
	}

	@Override
	public int getTodayCheckinDoneCount() {
	    return sqlsession.selectOne("adminReservation.getTodayCheckinDoneCount");
	}

	
	// 전체 객실 예약 리스트 조회
	@Override
    public List<Map<String, Object>> selectAdminReservationList(Map<String,Object> param) {

		return sqlsession.selectList("adminReservation.selectAdminReservationList", param);
    }

	
	@Override
	public List<Map<String, Object>> selectHotelList() {
		return sqlsession.selectList("adminReservation.selectHotelList");
	}

	@Override
	public List<Map<String, Object>> getNoShowList() {
	    return sqlsession.selectList("adminReservation.getNoShowList");
	}

	@Override
	public void updateNoShow(int reservationId) {
	    sqlsession.update("adminReservation.updateNoShow", reservationId);
	}

	// 🔥 서비스에서 상태 체크용
	@Override
	public Map<String, Object> findReservation(int reservationId) {
	    return sqlsession.selectOne("adminReservation.findReservation", reservationId);
	}

	// 추가(페이징)
	@Override
	public List<Map<String, Object>> selectAdminReservationListForExcel(Map<String, Object> param) {
	    return sqlsession.selectList("adminReservation.selectAdminReservationListForExcel", param);
	}
	

}