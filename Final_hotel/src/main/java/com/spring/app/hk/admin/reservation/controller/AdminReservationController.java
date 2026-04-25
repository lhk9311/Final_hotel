package com.spring.app.hk.admin.reservation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.spring.app.hk.admin.reservation.service.AdminReservationService;
import com.spring.app.hk.admin.reservation.service.ExcelService;
import com.spring.app.hk.admin.room.service.AdminRoomService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservation")
public class AdminReservationController {

	private final AdminReservationService reservationService;
	private final ExcelService excelService;
	private final AdminRoomService adminRoomService;

	// ======= 지점 관리자 ========= //
	/*
	 * // 예약 캘린더 (객실 배정)
	 * 
	 * @PreAuthorize("hasRole('ADMIN_BRANCH')")
	 * 
	 * @GetMapping("/calendar") public String reservationCalendar() {
	 * 
	 * return "hk/admin/reservation/calendar"; }
	 */

	// 예약 관리 페이지
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@GetMapping("/manage")
	public String reservationManage(Model model) {

	    List<Map<String, Object>> checkinList = reservationService.getTodayCheckinList();
	    List<Map<String, Object>> checkoutList = reservationService.getTodayCheckoutList();
	    List<Map<String, Object>> stayList = reservationService.getStayList();
	    List<Map<String, Object>> checkoutCompleteList = reservationService.getCheckoutCompleteList();
	    List<Map<String, Object>> overdueList = reservationService.getOverdueList();
	    List<Map<String, Object>> noShowList = reservationService.getNoShowList();
	    
	    // ===== KPI =====

	    // 🔥 오늘 체크인 "전체 대상 수" (변하지 않는 값)
	    int todayCheckinTotal = reservationService.getTodayCheckinTotalCount();

	    // 🔥 오늘 체크인 완료 수
	    int todayCheckinDone = reservationService.getTodayCheckinDoneCount();

	    int todayCheckoutCount = checkoutList.size();
	    int stayCount = stayList.size();
	    int overdueCount = overdueList.size();

	    int totalRoomCount = 100;
	    int occupancyRate = 0;

	    if(totalRoomCount > 0) {
	        occupancyRate = (int)(((double) stayCount / totalRoomCount) * 100);
	    }

	    // 🔥 체크인 진행률 (이제 절대 안깨짐)
	    int checkinProgress = 0;

	    if(todayCheckinTotal > 0) {
	        checkinProgress = (int)(((double) todayCheckinDone / todayCheckinTotal) * 100);
	    }

	    // ===== model =====
	    model.addAttribute("checkinList", checkinList);
	    model.addAttribute("checkoutList", checkoutList);
	    model.addAttribute("stayList", stayList);
	    model.addAttribute("checkoutCompleteList", checkoutCompleteList);
	    model.addAttribute("overdueList", overdueList);
	    model.addAttribute("noShowList", noShowList);

	    model.addAttribute("todayCheckinReserved", todayCheckinTotal); // 이름 유지
	    model.addAttribute("todayCheckinDone", todayCheckinDone);
	    model.addAttribute("todayCheckoutCount", todayCheckoutCount);
	    model.addAttribute("stayCount", stayCount);
	    model.addAttribute("occupancyRate", occupancyRate);
	    model.addAttribute("overdueCount", overdueCount);
	    model.addAttribute("checkinProgress", checkinProgress);

	    return "hk/admin/reservation/reservationManage";
	}

	// 체크인 처리
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@PostMapping("/checkin")
	public String checkin(@RequestParam("reservationId") int reservationId) {

		reservationService.checkinReservation(reservationId);

		return "redirect:/admin/reservation/manage";
	}

	// 체크아웃 처리
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@PostMapping("/checkout")
	public String checkout(@RequestParam("reservationId") int reservationId) {

		reservationService.checkoutReservation(reservationId);

		return "redirect:/admin/reservation/manage";
	}

	// 노쇼처리
	@PostMapping("/noshow")
	public String noShow(@RequestParam("reservationId") int reservationId) {

	    reservationService.noShowReservation(reservationId);

	    return "redirect:/admin/reservation/manage";
	}
	
	// ======= 총괄 관리자 ========= //
	// 전체 객실 예약 리스트 조회 + 검색
	@PreAuthorize("hasRole('ADMIN_HQ')")
	@GetMapping("/list")
	public String adminReservationList(
	        @RequestParam(value="name", required=false) String name,
	        @RequestParam(value="status", required=false) String status,
	        @RequestParam(value="hotelId", required=false) String hotelId,
	        Model model) {

	    Map<String,Object> param = new HashMap<>();
	    param.put("name", name);
	    param.put("status", status);
	    param.put("hotelId", hotelId);

	    List<Map<String,Object>> reservationList =
	            reservationService.selectAdminReservationList(param);

	    // 호텔 목록 추가
	    List<Map<String,Object>> hotelList =
	            reservationService.selectHotelList();

	    model.addAttribute("reservationList", reservationList);
	    model.addAttribute("hotelList", hotelList);

	    model.addAttribute("name", name);
	    model.addAttribute("status", status);
	    model.addAttribute("hotelId", hotelId);

	    return "hk/admin/reservation/adminreservationList";
	}
	
	
	// 엑셀 다운로드
	//@PreAuthorize("hasRole('ADMIN_HQ')")
	@GetMapping("/excel")
	public void downloadExcel(
	        @RequestParam(value="name", required=false) String name,
	        @RequestParam(value="status", required=false) String status,
	        HttpServletResponse response) throws Exception {

	    Map<String,Object> param = new HashMap<>();
	    param.put("name", name);
	    param.put("status", status);

	    int page = 1;
	    int pageSize = 1000; // 테스트용. 확인 끝나면 1000으로 변경 3으로 끊어서 조회 함.

	    List<Map<String,Object>> allList = new ArrayList<>();

	    while (true) {

	        int offset = (page - 1) * pageSize;

	        param.put("offset", offset);
	        param.put("limit", pageSize);

	        List<Map<String,Object>> list =
	            reservationService.selectAdminReservationListForExcel(param);

	        System.out.println(
	            "[Excel Paging] page=" + page
	            + ", offset=" + offset
	            + ", limit=" + pageSize
	            + ", 조회건수=" + list.size()
	        );

	        if (list.isEmpty()) break;

	        allList.addAll(list);

	        if (list.size() < pageSize) break;

	        page++;
	    }

	    excelService.downloadReservationExcel(allList, response);
	}
	
	
	
	
}