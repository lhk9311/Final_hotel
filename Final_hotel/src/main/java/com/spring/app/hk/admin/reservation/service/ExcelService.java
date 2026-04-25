package com.spring.app.hk.admin.reservation.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {
    void downloadReservationExcel(List<Map<String,Object>> list, 
                                   HttpServletResponse response) throws Exception;
    
    // 추가
    List<Map<String,Object>> parseRoomExcel(MultipartFile file) throws Exception;
    void downloadErrorExcel(List<Map<String,Object>> errorList,
                             HttpServletResponse response) throws Exception;
}