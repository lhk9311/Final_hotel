package com.spring.app.hk.admin.reservation.service;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelService_imple implements ExcelService {

	 private final AdminReservationService reservationService;
	
    private static final String[] HEADERS = {
        "예약번호", "회원", "호텔", "객실타입",
        "체크인", "체크아웃", "가격", "결제상태", "예약상태"
    };

    // 엑셀 다운로드
    @Override
    public void downloadReservationExcel(List<Map<String,Object>> list,
                                          HttpServletResponse response) throws Exception {

        SXSSFWorkbook wb = new SXSSFWorkbook(100);  // ← 변경
        wb.setCompressTempFiles(true);               // ← 추가

        try {
            SXSSFSheet sheet = wb.createSheet("예약목록");
            sheet.trackAllColumnsForAutoSizing();    // ← 추가

            // 헤더 스타일
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // 헤더 행
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행
            int rowNo = 1;
            for (Map<String,Object> r : list) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(String.valueOf(r.get("RESERVATION_ID")));
                row.createCell(1).setCellValue(String.valueOf(r.get("NAME")));
                row.createCell(2).setCellValue(String.valueOf(r.get("HOTEL_NAME")));
                row.createCell(3).setCellValue(String.valueOf(r.get("ROOM_TYPE_ID")));
                row.createCell(4).setCellValue(String.valueOf(r.get("CHECKIN_DATE")));
                row.createCell(5).setCellValue(String.valueOf(r.get("CHECKOUT_DATE")));
                row.createCell(6).setCellValue(String.valueOf(r.get("TOTAL_PRICE")));
                row.createCell(7).setCellValue(String.valueOf(r.get("PAYMENT_STATUS")));
                row.createCell(8).setCellValue(String.valueOf(r.get("RESERVATION_STATUS")));
            }

            // autoSizeColumn은 데이터 다 넣은 후에 호출
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);             // ← 데이터 다 넣고 나서 호출
            }

            // 응답
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=reservation_list.xlsx");

            wb.write(response.getOutputStream());

        } finally {
            wb.dispose();  // ← SXSSFWorkbook 은 임시파일 정리 필수
            wb.close();
        }
            
    }
    
 // ── 업로드 파싱 ──
    @Override
    public List<Map<String,Object>> parseRoomExcel(MultipartFile file) throws Exception {

        List<Map<String,Object>> resultList = new ArrayList<>();

        Workbook wb = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = wb.getSheetAt(0);

        // 1행은 헤더니까 2행부터 읽기
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String,Object> map = new HashMap<>();
            map.put("rowNum", i + 1);  // 몇 번째 행인지 (사용자 기준)

            // 셀 읽기
            map.put("fkHotelId", getCellValue(row, 0));
            map.put("roomName", getCellValue(row, 1));
            map.put("roomGrade", getCellValue(row, 2));
            map.put("bedType", getCellValue(row, 3));
            map.put("viewType", getCellValue(row, 4));
            map.put("roomSize", getCellValue(row, 5));
            map.put("maxCapacity", getCellValue(row, 6));
            map.put("totalCount", getCellValue(row, 7));
            map.put("basePrice", getCellValue(row, 8));

            // ── 유효성 검증 ──
            List<String> errors = new ArrayList<>();

            if (isEmpty(map.get("fkHotelId")))   errors.add("호텔ID 누락");
            if (isEmpty(map.get("roomName")))     errors.add("객실명 누락");
            if (isEmpty(map.get("roomGrade")))    errors.add("등급 누락");
            if (isEmpty(map.get("bedType")))      errors.add("침대타입 누락");
            if (isEmpty(map.get("viewType")))     errors.add("전망타입 누락");
            if (isEmpty(map.get("maxCapacity")))  errors.add("최대인원 누락");
            if (isEmpty(map.get("totalCount")))   errors.add("총객실수 누락");
            if (isEmpty(map.get("basePrice")))    errors.add("기본가격 누락");

            // 숫자 형식 체크
            if (!isEmpty(map.get("fkHotelId")) && !isNumeric(map.get("fkHotelId")))
                errors.add("호텔ID 숫자 형식 오류");
            if (!isEmpty(map.get("basePrice")) && !isNumeric(map.get("basePrice")))
                errors.add("기본가격 숫자 형식 오류");
            if (!isEmpty(map.get("maxCapacity")) && !isNumeric(map.get("maxCapacity")))
                errors.add("최대인원 숫자 형식 오류");

            map.put("errors", errors);  // 오류 목록 담기
            resultList.add(map);
        }

        wb.close();
        return resultList;
    }


    // ── 오류 행만 엑셀로 반환 ──
    @Override
    public void downloadErrorExcel(List<Map<String,Object>> errorList,
                                     HttpServletResponse response) throws Exception {

        SXSSFWorkbook wb = new SXSSFWorkbook(100);

        try {
            SXSSFSheet sheet = wb.createSheet("오류목록");
            sheet.trackAllColumnsForAutoSizing();

            // 헤더 스타일 (빨간색으로 오류 강조)
            CellStyle errorHeaderStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            errorHeaderStyle.setFont(font);
            errorHeaderStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 헤더 행
            String[] headers = {
                "행번호", "호텔ID", "객실명", "등급", "침대타입",
                "전망타입", "객실크기", "최대인원", "총객실수", "기본가격", "오류내용"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(errorHeaderStyle);
            }

            // 오류 데이터 행
            int rowNo = 1;
            for (Map<String,Object> r : errorList) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(String.valueOf(r.get("rowNum")));
                row.createCell(1).setCellValue(String.valueOf(r.get("fkHotelId")));
                row.createCell(2).setCellValue(String.valueOf(r.get("roomName")));
                row.createCell(3).setCellValue(String.valueOf(r.get("roomGrade")));
                row.createCell(4).setCellValue(String.valueOf(r.get("bedType")));
                row.createCell(5).setCellValue(String.valueOf(r.get("viewType")));
                row.createCell(6).setCellValue(String.valueOf(r.get("roomSize")));
                row.createCell(7).setCellValue(String.valueOf(r.get("maxCapacity")));
                row.createCell(8).setCellValue(String.valueOf(r.get("totalCount")));
                row.createCell(9).setCellValue(String.valueOf(r.get("basePrice")));

                // 오류 내용 합쳐서 한 셀에
                List<String> errors = (List<String>) r.get("errors");
                row.createCell(10).setCellValue(String.join(", ", errors));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=error_list.xlsx");

            wb.write(response.getOutputStream());

        } finally {
            wb.dispose();
            wb.close();
        }
    }


    // ── 헬퍼 메서드 ──
    private String getCellValue(Row row, int idx) {
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isEmpty(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }

    private boolean isNumeric(Object value) {
        try {
            Double.parseDouble(value.toString().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
  
    // 개선
    @Override
    public void downloadReservationExcelByPaging(Map<String, Object> param,
                                                 HttpServletResponse response) throws Exception {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=reservation_list.xlsx");

        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        wb.setCompressTempFiles(true);

        try {
            SXSSFSheet sheet = wb.createSheet("예약목록");
            sheet.trackAllColumnsForAutoSizing();

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            int rowNo = 0;

            Row header = sheet.createRow(rowNo++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int page = 1;
            int pageSize = 1000;  // 테스트용... 3 -> 1000

            while (true) {
                int offset = (page - 1) * pageSize;

                param.put("offset", offset);
                param.put("limit", pageSize);

                List<Map<String, Object>> list =
                        reservationService.selectAdminReservationListForExcel(param);

                System.out.println(
                    "[Excel Paging] page=" + page
                    + ", offset=" + offset
                    + ", limit=" + pageSize
                    + ", 조회건수=" + list.size()
                );

                if (list.isEmpty()) {
                    break;
                }

                for (Map<String, Object> r : list) { // 즉시 작성
                    Row row = sheet.createRow(rowNo++);

                    row.createCell(0).setCellValue(String.valueOf(r.get("RESERVATION_ID")));
                    row.createCell(1).setCellValue(String.valueOf(r.get("NAME")));
                    row.createCell(2).setCellValue(String.valueOf(r.get("HOTEL_NAME")));
                    row.createCell(3).setCellValue(String.valueOf(r.get("ROOM_TYPE_ID")));
                    row.createCell(4).setCellValue(String.valueOf(r.get("CHECKIN_DATE")));
                    row.createCell(5).setCellValue(String.valueOf(r.get("CHECKOUT_DATE")));
                    row.createCell(6).setCellValue(String.valueOf(r.get("TOTAL_PRICE")));
                    row.createCell(7).setCellValue(String.valueOf(r.get("PAYMENT_STATUS")));
                    row.createCell(8).setCellValue(String.valueOf(r.get("RESERVATION_STATUS")));
                }

                if (list.size() < pageSize) {
                    break;
                }

                page++;
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(response.getOutputStream());

        } finally {
            wb.dispose();
            wb.close();
        }
    }

}