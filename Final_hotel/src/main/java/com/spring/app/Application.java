package com.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   //  스프링 스케줄러 ss
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}