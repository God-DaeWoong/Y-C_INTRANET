# YNC Intranet System (사내 인트라넷)

사내 업무 통합 관리 시스템 - 일정/휴가, 경비보고서, 문서결재, 알림 기능을 제공합니다.

**Current Version: v0.28**

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **ORM**: MyBatis 3.0
- **Database**: Oracle Database (Oracle XE)
- **Build Tool**: Maven

### Frontend
- **Framework**: Vanilla JavaScript (SPA)
- **Calendar Library**: FullCalendar 6.1.10
- **Styling**: Custom CSS + Bootstrap 5

---

## 주요 기능

### 1. 사용자 인증 및 권한 관리
- 로그인/로그아웃 (Session 기반)
- 역할 기반 접근 제어 (ADMIN/USER)
- 관리자 페이지 분리

### 2. 일정 및 휴가 관리
- **일정 유형**:
  - 휴가: 연차, 오전반차, 오후반차, 병가, 경조휴가, 공가, 대체휴무, 기타휴가
  - 일정: 회의, 외근, 출장, 교육, 개인일정, 기타일정
  - 특수: 휴일근무 (대체휴무 자동 생성)
- FullCalendar 기반 월/주/일 뷰
- 부서별/멤버별 필터링
- 잔여 연차 자동 계산

### 3. 승인 워크플로우
- 상태: DRAFT → SUBMITTED → APPROVED/REJECTED
- 승인자 지정 및 승인/반려 처리
- 반려 사유 입력 (선택)
- 내 결재 대기함

### 4. 경비보고서 시스템
- 경비 항목 등록 (사용일, 계정, 금액, 복지비 여부)
- 월별 경비보고서 생성/조회
- **경비 신청 워크플로우**:
  - EXPENSE_ITEMS_INTRANET (상세) → EXPENSE_ITEMS (마스터)
  - 신청 시 마스터 테이블 동기화
- **미확인 경비 관리**:
  - EXPENSE_ITEM_READ_STATUS 테이블
  - 읽음/안읽음 상태 관리
  - 알림 배지 표시

### 5. 문서 작성 시스템
- 문서 작성/수정/삭제
- 파일 첨부 (UUID 없이 원본 파일명 유지)
- 문서 승인 워크플로우

### 6. 알림 시스템
- 실시간 알림 (30초 폴링)
- 알림 벨 아이콘 (모든 페이지 공통)
- 읽음/안읽음 상태 관리
- 알림 유형: 승인요청, 승인완료, 반려, 경비알림

---

## 데이터베이스 구조

### 핵심 테이블 (15+)

```
사용자/조직
├── MEMBERS_INTRANET         - 사용자 정보
└── DEPARTMENTS_INTRANET     - 부서 정보

일정/휴가
├── SCHEDULES_INTRANET       - 일정/휴가 통합
└── SCHEDULE_APPROVALS_INTRANET - 일정 승인

경비보고서
├── EXPENSE_REPORTS_INTRANET - 경비보고서 헤더
├── EXPENSE_ITEMS_INTRANET   - 경비 항목 (상세)
├── EXPENSE_ITEMS            - 경비 마스터 (신청용)
└── EXPENSE_ITEM_READ_STATUS - 경비 읽음 상태

문서
├── DOCUMENTS_INTRANET       - 문서 정보
├── DOCUMENT_APPROVALS_INTRANET - 문서 승인
└── DOCUMENT_FILES_INTRANET  - 첨부파일

알림
└── NOTIFICATIONS_INTRANET   - 알림 정보
```

---

## 프로젝트 구조

```
yncIntranet/
├── src/main/java/com/ync/
│   ├── intranet/                    # 인트라넷 메인
│   │   ├── controller/              # REST API 컨트롤러
│   │   ├── service/                 # 비즈니스 로직
│   │   ├── mapper/                  # MyBatis 매퍼 인터페이스
│   │   ├── domain/                  # 엔티티/도메인 클래스
│   │   └── dto/                     # 데이터 전송 객체
│   └── schedule/                    # 스케줄 모듈 (레거시)
│
├── src/main/resources/
│   ├── mapper/
│   │   └── intranet/                # MyBatis XML 매퍼
│   ├── static/
│   │   ├── *.html                   # 페이지 파일들
│   │   ├── css/                     # 스타일시트
│   │   ├── js/                      # JavaScript
│   │   └── uploads/                 # 업로드 파일
│   └── application.yml              # 설정 파일
│
├── database/
│   └── schema-oracle.sql            # Oracle DDL
│
├── INTRANET_SETUP.md                # 상세 개발 문서
├── Intranet_Dev_board.md            # 개발 위원회 규칙
└── README.md                        # 이 파일
```

---

## 설치 및 실행

### 1. 사전 요구사항
- Java 17+
- Maven 3.6+
- Oracle Database (Oracle XE 권장)

### 2. 데이터베이스 설정

```bash
# Oracle 접속
sqlplus system/password@localhost:1522/XEPDB1

# 스키마 생성
@database/schema-oracle.sql
```

### 3. 애플리케이션 설정

`src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1522/XEPDB1
    username: system
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
```

### 4. 빌드 및 실행

```bash
# 빌드
mvn clean package

# 실행
mvn spring-boot:run

# 또는 JAR 실행
java -jar target/intranet-1.0.0.jar
```

### 5. 접속

```
http://localhost:8080/
```

---

## 주요 API 엔드포인트

### 인증
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/intranet/auth/login` | 로그인 |
| POST | `/api/intranet/auth/logout` | 로그아웃 |
| GET | `/api/intranet/auth/me` | 현재 사용자 정보 |

### 일정/휴가
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/intranet/schedules` | 일정 목록 |
| POST | `/api/intranet/schedules` | 일정 생성 |
| PUT | `/api/intranet/schedules/{id}` | 일정 수정 |
| DELETE | `/api/intranet/schedules/{id}` | 일정 삭제 |
| POST | `/api/intranet/schedules/{id}/submit` | 승인 요청 |
| POST | `/api/intranet/schedules/{id}/approve` | 승인 |
| POST | `/api/intranet/schedules/{id}/reject` | 반려 |

### 경비보고서
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/intranet/expense-items` | 경비 항목 목록 |
| POST | `/api/intranet/expense-items` | 경비 항목 등록 |
| POST | `/api/intranet/expense-items/submit` | 경비 신청 |
| GET | `/api/intranet/expense-read-status/unread` | 미확인 경비 |
| POST | `/api/intranet/expense-read-status/mark-read` | 읽음 처리 |

### 알림
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/intranet/notifications` | 알림 목록 |
| GET | `/api/intranet/notifications/unread-count` | 미읽음 수 |
| POST | `/api/intranet/notifications/{id}/read` | 읽음 처리 |

---

## 페이지 구성

### 사용자 페이지
- `/login.html` - 로그인
- `/main.html` - 메인 대시보드
- `/schedule-calendar.html` - 일정/휴가 캘린더
- `/expense-report.html` - 경비보고서
- `/document-write.html` - 문서 작성
- `/my-approvals.html` - 내 결재 대기함
- `/notifications.html` - 알림 목록

### 관리자 페이지
- `/admin/admin-main.html` - 관리자 대시보드
- `/admin/admin-members.html` - 사용자 관리
- `/admin/admin-departments.html` - 부서 관리

---

## 버전 히스토리

| Version | Date | Description |
|---------|------|-------------|
| v0.28 | 2026-01-21 | 경비 신청/미확인 경비 관리 시스템 |
| v0.27 | 2026-01-20 | 문서 작성, 내 결재 대기함 |
| v0.26 | 2026-01-19 | 알림 시스템 전체 적용 |
| v0.25 | 2026-01-18 | 관리자 페이지 기능 추가 |

전체 버전 히스토리는 [INTRANET_SETUP.md](INTRANET_SETUP.md) 참조

---

## 개발 위원회

본 프로젝트는 6인 개발 위원회 합의 구조로 의사결정합니다.

- **충원** - 시스템 아키텍트 (위원장)
- **의섭** - DB 스페셜리스트
- **준영** - 클라우드/인프라
- **현수** - Delivery Lead
- **승주** - 리스크 매니저
- **은지** - UI/UX 스페셜리스트

상세 규칙: [Intranet_Dev_board.md](Intranet_Dev_board.md)

---

## 문제 해결

### 데이터베이스 연결 오류
- Oracle Database 실행 상태 확인
- 연결 정보 (호스트, 포트, 서비스명) 확인
- 사용자 권한 확인

### 빌드 오류
```bash
mvn clean install -U
```

### 포트 충돌
`application.yml`에서 포트 변경:
```yaml
server:
  port: 8081
```

---

## 라이선스

YNC Development Team - 내부 교육 및 개발 목적

---

## 작성자

YNC Development Team
