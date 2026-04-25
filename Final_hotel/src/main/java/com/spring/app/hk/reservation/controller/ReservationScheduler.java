package com.spring.app.hk.reservation.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.spring.app.hk.reservation.service.ReservationService_imple;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService_imple reservationService;

    // [운영용] 매일 오전 9시 30분 이지만 시연영상에서는 30초씩 실행되도록 설정함.
	

	 @Scheduled(cron = "30 9 0 * * *", zone = "Asia/Seoul") public void
	 sendReminder() { System.out.println("⏰ 자동 스케줄러 실행");
	 reservationService.sendCheckinReminderMail(); }
	 
	 
/*
 * // 리마인드 메일 30초씩 실행해보도록 하겠습니다.
 * 
 * @Scheduled(fixedRate = 30000) // 30초 = 30000ms public void sendReminder() {
 * System.out.println("⏰ 자동 스케줄러 실행");
 * reservationService.sendCheckinReminderMail(); } }
 */}