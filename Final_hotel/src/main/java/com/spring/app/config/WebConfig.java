package com.spring.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
   // URL 경로와 외부경로를 매핑시켜주는 설정 클래스 생성하기
   
   @Value("${file.images-dir}") 
   private String imagesDir;          // 이메일 작성시 첨부파일의 경로를 잡아주는 것이다.

   
   @Override
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
      
       registry.addResourceHandler("/file_images/**")
         	   .addResourceLocations("file:./file_images/");
      // Spring 은 다음의 순서대로 찾는다. 
      // 제일먼저, 외부 업로드 폴더를 먼저 검색하고(file_images/쉐보레.jpg) 있으면 이것을 사용하고,
      // 만약에 없으면 static 을 검색한다.(static/images/쉐보레.jpg)
      // 그리고 스프링시큐리티 설정파일인 com.spring.app.security.config.SecurityConfig 에서 excludeUri 에 "/images/**" 을 추가해 주어야 한다. 
      
      // 다이닝 매장 및 음식 이미지 경로 
        registry.addResourceHandler("/images/dining/**")
                .addResourceLocations("classpath:/static/images/dining/");

        registry.addResourceHandler("/images/food/**")
                .addResourceLocations("classpath:/static/images/dining/");

        registry.addResourceHandler("/files/menu/**")
                .addResourceLocations("classpath:/static/images/menu/");
   
   }
}
