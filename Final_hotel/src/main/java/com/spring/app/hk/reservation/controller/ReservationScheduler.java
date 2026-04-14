package com.spring.app.hk.reservation.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.spring.app.hk.reservation.service.ReservationService_imple;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService_imple reservationService;

    // [운영용] 매일 오전 9시 30분
    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Seoul")
    public void sendReminder() {
        System.out.println("⏰ 자동 스케줄러 실행");
        reservationService.sendCheckinReminderMail();
    }
}