# 🏨 CIEL 호텔 PMS (Hotel Management System)
> **파이널 프로젝트 (팀 프로젝트 - 4인 / 기여도 30%)**
> 
> 효율적인 객실 예약 현황과 재고를 실시간으로 관리하는 **호텔 예약 관리 시스템(PMS)** 입니다. 대규모 예약 요청 시 발생할 수 있는 데이터 경합 문제(Race Condition) 해결과 Spring Security를 활용한 보안 아키텍처 설계에 집중하여 프로젝트를 수행했습니다.

<br>

## 1. 📅 프로젝트 기간
- 2026.02.24 ~ 2026.03.31 (총 5주)

<br>

## 2. 🛠 기술 스택

### 💻 Backend
<img src="https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white"/> <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/> <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge&logo=mybatis&logoColor=white"/> <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>

### 🗄️ Database
<img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white"/>

### 🌐 Frontend
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/> <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white"/> <img src="https://img.shields.io/badge/AJAX-000000?style=for-the-badge&logo=javascript&logoColor=white"/>

### 🔧 Infra & DevOps
<img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>

<br>

## 👤 3. 담당 역할 및 기여도 (My Role)
- **객실 예약 도메인 설계 및 핵심 로직 구현**
  - 객실 타입, 날짜별 재고, 시즌별 요금 정책을 반영한 비즈니스 규칙 설계
  - **DB Row-Level Lock**을 활용한 중복 예약 방지(Race Condition 해결)
- **인증 및 보안 시스템 구축**
  - Spring Security와 JWT를 연동한 토큰 기반 인증 아키텍처 구현
  - CSRF 토큰 검증 로직 최적화 및 AJAX 통신 보안 강화
- **외부 API 연동 및 검증**
  - **PortOne(Iamport) 결제 API** 연동 및 서버 측 결제 금액 위변조 검증 로직 구현
  - CoolSMS API를 활용한 예약 알림 자동화 시스템 구축
- **아키텍처 고도화**
  - Monolithic 구조에서 게시판 기능을 별도 서비스로 분리하는 **MSA 전환 시도** (API Gateway 라우팅 설정)

<br>

## 🚀 4. 핵심 기능 및 트러블슈팅

### ✅ DB Row-Level Lock을 통한 예약 동시성 제어
- **문제 상황:** 동일 객실/일정에 대해 다수 사용자가 동시 결제 시 중복 예약(Overbooking) 발생 가능성 존재.
- **해결 방안:** 재고 조회 시 `SELECT ... FOR UPDATE` 구문을 사용하여 특정 Row에 **비관적 락(Pessimistic Lock)** 적용. 트랜잭션 동안 타 요청의 접근을 대기시켜 데이터 정합성 확보.
- **결과:** 동시성 요청 환경에서도 데이터 불일치 이슈를 원천 차단함.

### ✅ 결제 금액 위변조 방지를 위한 서버 측 검증
- **문제 상황:** 클라이언트 측에서 결제 요청 시 금액을 조작할 수 있는 보안 취약점 존재.
- **해결 방안:** 결제 성공 후 PortOne REST API를 호출하여 서버 측에서 실제 결제된 금액과 DB의 예약 정보를 대조하는 검증 파이프라인 구축.
- **결과:** 결제 데이터 신뢰성을 확보하고 비정상적인 결제 시도를 차단함.

### ✅ JWT 전환 후 AJAX POST 요청 차단 이슈 해결
- **문제 상황:** 세션 기반 인증에서 JWT로 전환 후, CSRF 토큰 미포함으로 인해 AJAX POST 요청이 Filter Chain에서 거부됨.
- **해결 방안:** 공통 레이아웃의 메타 태그에 CSRF 정보를 저장하고, **JS fetch/ajax 헤더에 토큰을 주입**하도록 수정하여 인증 안정성 확보.
- **결과:** 보안 설정을 유지하면서 비동기 통신을 정상화함.

<br>

## 📂 5. 프로젝트 구조 (My Part)
```text
src/main/java/com/hotel/ciel
 ├── admin          # 관리자 시스템 (HQ 대시보드, 호텔 등록 및 승인)
 ├── booking        # 예약 도메인 (재고 관리, 비관적 락 적용 로직)
 ├── common         # 공통 보안(JWT Filter, CSRF Config) 및 유틸리티
 ├── payment        # 결제 검증 (PortOne API 연동 및 데이터 대조)
 └── member         # 회원 관리 (인증 및 OAuth2 소셜 로그인 처리)
