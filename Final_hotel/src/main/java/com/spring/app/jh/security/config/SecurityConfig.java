package com.spring.app.jh.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.spring.app.jh.security.jwt.JwtAuthenticationFilter;
import com.spring.app.jh.security.jwt.JwtTokenProvider;
import com.spring.app.jh.security.loginsuccess.MemberAuthenticationSuccessHandler;
import com.spring.app.jh.security.service.AdminUserDetailsService;
import com.spring.app.jh.security.service.CustomOAuth2UserService;
import com.spring.app.jh.security.service.MemberUserDetailsService;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티02-JWT하이브리드) ===== */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // =====================================================================
    // 0) JWT 핵심 객체 주입
    // =====================================================================
    private final JwtTokenProvider jwtTokenProvider;


    // =====================================================================
    // 1) 공통: 비밀번호 암호화
    // =====================================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public MemberAuthenticationSuccessHandler memberAuthenticationSuccessHandler() {
        return new MemberAuthenticationSuccessHandler("/index");
    }


    // =====================================================================
    // 2) 공통: 403(권한 부족) 처리
    // =====================================================================
    @Bean
    AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect(request.getContextPath() + "/security/noAuthorized");
        };
    }


    // =====================================================================
    // 3) 회원/관리자 인증 Provider 구성
    // =====================================================================
    /*
        [중요]
        AuthenticationManager Bean 을 2개 만들면
        Spring Security 내부 HttpSecurity 생성 시 "전역 AuthenticationManager" 충돌이 발생할 수 있다.

        따라서 현재 구조에서는
        - memberAuthProvider
        - adminAuthProvider
        만 Bean 으로 등록하고,
        실제 로그인 인증은 JwtAuthService_imple 에서 Provider.authenticate(...) 를 직접 호출한다.
     */
    @Bean
    public DaoAuthenticationProvider memberAuthProvider(MemberUserDetailsService memberUserDetailsService,
                                                        PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider(AdminUserDetailsService adminUserDetailsService,
                                                       PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    // =====================================================================
    // 4) 관리자 체인(adminChain)
    // =====================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity httpSecurity) throws Exception {

        AuthenticationEntryPoint adminEntryPoint = (request, response, authException) -> {
            response.sendRedirect(request.getContextPath() + "/admin/login");
        };

        httpSecurity
            .securityMatcher("/admin/**", "/api/auth/admin/**")

            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )

            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers("/admin/login").permitAll()
                .requestMatchers("/api/auth/admin/login", "/api/auth/admin/refresh").permitAll()
                .requestMatchers("/error", "/favicon.ico").permitAll()
                .anyRequest().permitAll()
            )

            .exceptionHandling(exceptionConfig -> exceptionConfig
                .authenticationEntryPoint(adminEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                             UsernamePasswordAuthenticationFilter.class)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .headers(headerConfig -> headerConfig
                .frameOptions(frame -> frame.sameOrigin())
            );

        return httpSecurity.build();
    }


    // =====================================================================
    // 5) 일반 사이트 체인(memberChain)
    // =====================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain memberChain(HttpSecurity httpSecurity,
								    		CustomOAuth2UserService customOAuth2UserService,
								            MemberAuthenticationSuccessHandler memberAuthenticationSuccessHandler) throws Exception {

        AuthenticationEntryPoint memberEntryPoint = (request, response, authException) -> {
            response.sendRedirect(request.getContextPath() + "/security/noAuthenticated");
        };

        String[] excludeUri = {
            "/",
            "/index",
            "/security/everybody",
            "/security/noAuthenticated",
            "/security/noAuthorized",

            "/security/memberRegister",
            "/security/member_id_check",
            "/security/member_id_find",
            "/security/member_pw_find",
            "/security/emailDuplicateCheck",
            "/security/agree",
            "/security/memberRegisterEnd",
            "/security/login",

            "/guest/login",
            "/guest/loginEnd",
            "/guest/logout",

            "/oauth2/**",
            "/login/oauth2/**",

            "/board/list",
            "/board/view",
            "/board/view_2",
            "/board/readComment",
            "/board/wordSearchShow",
            "/board/commentList",
            "/board/ddCommentList",

            "/opendata/**",
            "/upload/**",
            "/photoupload/**",
            "/emailattachfile/**",
            "/hk_images/**",
            "/file_images/**",
            "/product/list",
            "/room/**",
            "/js_images/**",
            "/notice/list",
            "/notice/detail/**",
            "/cs/**",
            "/hotel/location",
            
            "/dining/**",
            "/images/dining/**",
            "/images/food/**",
            "/files/menu/**",
            "/search"
           
        };

        httpSecurity
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )

            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers("/admin/**").denyAll()
                .requestMatchers("/api/auth/member/login", "/api/auth/member/refresh").permitAll()
                .requestMatchers(excludeUri).permitAll()
                .requestMatchers("/notice/write", "/notice/edit/**", "/notice/delete")
                    .hasAnyRole("ADMIN_HQ", "ADMIN_BRANCH")
                .requestMatchers("/promotion/reserve").authenticated()
                .requestMatchers("/promotion/**").permitAll()
                .requestMatchers("/cs/qnaWrite", "/cs/qnaDelete").authenticated()
                .requestMatchers("/reservation/**").permitAll()
                .requestMatchers("/payment/**").permitAll()
                .requestMatchers("/shuttle/**").permitAll()
                .requestMatchers("/security/special/**").hasAnyRole("ADMIN_HQ", "ADMIN_BRANCH", "USER_SPECIAL")
                .requestMatchers("/security/admin/**").hasAnyRole("ADMIN_HQ", "ADMIN_BRANCH")
                .requestMatchers("/emp/**").hasAnyRole("ADMIN_HQ", "ADMIN_BRANCH")
                .requestMatchers("/error", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )

            .oauth2Login(oauth2 -> oauth2
        	    .loginPage("/security/login")
        	    .userInfoEndpoint(userInfo -> userInfo
        	        .userService(customOAuth2UserService)
        	    )
        	    .successHandler(memberAuthenticationSuccessHandler)
        	)

            .exceptionHandling(exceptionConfig -> exceptionConfig
                .authenticationEntryPoint(memberEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                             UsernamePasswordAuthenticationFilter.class)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .headers(headerConfig -> headerConfig
                .frameOptions(frame -> frame.sameOrigin())
            );

        return httpSecurity.build();
    }


    // =====================================================================
    // 6) 정적 리소스는 보안 필터 자체를 타지 않게 제외
    // =====================================================================
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/bootstrap-4.6.2-dist/**",
                             "/css/**",
                             "/fullcalendar_5.10.1/**",
                             "/Highcharts-10.3.1/**",
                             "/images/**",
                             "/files/**",
                             "/jquery-ui-1.13.1.custom/**",
                             "/js/**",
                             "/smarteditor/**",
                             "/resources/photo_upload/**",
                             "/swagger-ui/**",
                             "/swagger-ui.html",
                             "/v3/api-docs/**");
    }
    
    
}
