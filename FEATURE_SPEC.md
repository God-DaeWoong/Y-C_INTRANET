# 기능명세서 (Feature Specification)

## YNC 사내 인트라넷 시스템 (YNC Intranet System)

**문서버전:** v1.0
**최종수정:** 2026-01-22
**작성:** YNC Development Team

---

## 1. 개요

### 1.1 시스템 목적
- 사내 구성원의 일정/휴가, 경비, 문서결재를 통합 관리하는 인트라넷 플랫폼
- 현재는 내부 Session 기반 인증, 향후 네이버웍스(Naver Works) OAuth 연동 예정
- 사내 구성원은 로그인 후 마이페이지에서 연차 현황 및 경비 사용내역 확인
- 표준 양식을 통한 근태신청, 경비신청 등 결재 프로세스 지원

### 1.2 상태 범례

| 표시 | 의미 |
|------|------|
| ✅ | 구현 완료 |
| 🔄 | 부분 구현 / 진행중 |
| ⬚ | 미구현 (To-Be) |

---

## 2. 주요 기능 요약

| 구분 | 기능 | 주요내용 | 상태 |
|------|------|----------|------|
| 인증 | 내부 로그인 | Session 기반 ID/PW 인증 | ✅ |
| 인증 | 네이버웍스 연동 로그인 | OAuth 기반 인증 및 조직도 연계 | ⬚ |
| 마이페이지 | 연차 현황 | 연차 사용/잔여일 표시 | ✅ |
| 마이페이지 | 경비 사용현황 | 경비 사용내역 표시 | ✅ |
| 마이페이지 | 자기개발비 현황 | 연간 한도, 사용금액, 잔액 | ⬚ |
| 일정관리 | 캘린더 뷰 | FullCalendar 기반 월/주/일 뷰 | ✅ |
| 일정관리 | 일정/휴가 등록 | 다양한 일정 유형 지원 | ✅ |
| 결재 | 근태신청 | 휴가/연차 승인 요청 | ✅ |
| 결재 | 경비신청 | 경비보고서 제출 | ✅ |
| 결재 | 일반기안 | 표준양식 기반 기안 | ⬚ |
| 결재행동 | 상신/승인/반려 | 기본 결재 프로세스 | ✅ |
| 결재행동 | 재상신/상신취소 | 반려 후 재상신, 결재전 취소 | 🔄 |
| 경고 | 잔여일 초과 경고 | 연차 초과 시 워닝 표시 | ⬚ |
| 알림 | 결재 상태 알림 | 웹 알림 (폴링 방식) | ✅ |
| 알림 | 이메일/웍스 알림 | 외부 알림 연동 | ⬚ |
| 문서관리 | 문서 작성/저장 | 문서 CRUD + 첨부파일 | ✅ |
| 문서관리 | PDF 자동 저장 | 완료 문서 PDF 변환 | ⬚ |
| 권한 | Role 기반 제어 | ADMIN/USER 2단계 | ✅ |
| 권한 | 세분화 권한 | 부서장, 팀장, 대결자 등 | ⬚ |

---

## 3. 기능 상세

### 3.1 인증

#### 3.1.1 내부 로그인 ✅
- **인증 방식:** Session 기반 ID/Password
- **주요 기능:**
  - 사용자 로그인/로그아웃
  - 세션 유지 (서버 메모리)
  - 로그인 실패 메시지 표시
- **관련 파일:**
  - [login.html](src/main/resources/static/login.html)
  - [AuthIntranetController.java](src/main/java/com/ync/intranet/controller/AuthIntranetController.java)

#### 3.1.2 네이버웍스 연동 로그인 ⬚
- **인증 방식:** OAuth2 + Naver Works (OIDC/SSO)
- **목표 기능:**
  - 네이버웍스 계정으로 SSO 로그인
  - 조직도 정보(부서, 직책) 자동 연계
  - 사용자 정보 세션/JWT 캐시
  - 미등록 사용자 자동 생성
- **비고:** 네이버웍스 사용자 정보를 신뢰 소스로 사용

---

### 3.2 마이페이지

#### 3.2.1 연차 현황 ✅
- **항목:** 총 연차, 사용일수, 잔여일수
- **기능:**
  - 개인별 연차 현황 조회
  - 실시간 계산 (부여 - 사용 = 잔여)
- **관련 파일:**
  - [schedule-calendar.html](src/main/resources/static/schedule-calendar.html) (leaveGranted, leaveUsed, leaveRemaining)

#### 3.2.2 경비 사용현황 ✅
- **항목:** 월별 경비 내역, 총 사용금액
- **기능:**
  - 경비 항목 목록 조회
  - 복지비 구분 표시
- **관련 파일:**
  - [expense-report.html](src/main/resources/static/expense-report.html)

#### 3.2.3 자기개발비 현황 ⬚
- **항목:** 연간 한도, 사용금액, 잔액
- **목표 기능:**
  - 자기개발비 별도 트래킹
  - 사용 이력 확인

---

### 3.3 일정/휴가 관리

#### 3.3.1 캘린더 뷰 ✅
- **구현:** FullCalendar 6.1.10
- **뷰 모드:** 월간(dayGridMonth), 주간(timeGridWeek), 일간(timeGridDay)
- **기능:**
  - 일정 드래그 앤 드롭
  - 날짜 클릭 시 일정 등록
  - 부서별/멤버별 필터링

#### 3.3.2 일정 유형 ✅

| 대분류 | 소분류 | 설명 |
|--------|--------|------|
| 휴가 | 연차 | 종일 휴가 (1일 차감) |
| 휴가 | 오전반차 | 오전 반차 (0.5일 차감) |
| 휴가 | 오후반차 | 오후 반차 (0.5일 차감) |
| 휴가 | 병가 | 병가 (차감 없음) |
| 휴가 | 경조휴가 | 경조사 휴가 |
| 휴가 | 공가 | 공적 휴가 |
| 휴가 | 대체휴무 | 휴일근무 대체 |
| 휴가 | 기타휴가 | 기타 |
| 일정 | 회의 | 회의 일정 |
| 일정 | 외근 | 외근 |
| 일정 | 출장 | 출장 |
| 일정 | 교육 | 교육/세미나 |
| 일정 | 개인일정 | 개인 일정 |
| 일정 | 기타일정 | 기타 |
| 특수 | 휴일근무 | 휴일 출근 (대체휴무 자동 생성) |

---

### 3.4 결재 기능

#### 3.4.1 결재 종류(문서유형)

| 유형 | 설명 | 상태 |
|------|------|------|
| 근태신청 | 휴가/연차/외근 등 승인 요청 | ✅ |
| 경비신청 | 경비보고서 제출 및 승인 | ✅ |
| 일반기안 | 표준양식 기반 일반 기안 | ⬚ |

#### 3.4.2 결재 상태 흐름 ✅

```
DRAFT (임시저장)
   ↓ 상신(Submit)
SUBMITTED (상신됨)
   ↓ 승인(Approve) / 반려(Reject)
APPROVED (승인완료) / REJECTED (반려)
   ↓ (반려 시)
DRAFT → 재상신 가능
```

#### 3.4.3 결재 행위(Action)

| Action | 설명 | 상태 |
|--------|------|------|
| 상신(Submit) | 기안자가 문서 제출 | ✅ |
| 승인(Approve) | 결재자가 승인 완료 | ✅ |
| 반려(Reject) | 결재자가 반려 (사유 선택) | ✅ |
| 재상신(Resubmit) | 반려 후 수정하여 재상신 | 🔄 |
| 상신취소(Cancel) | 상신 후 결재 전 취소 | 🔄 |

#### 3.4.4 결재선 관리

| 기능 | 상태 |
|------|------|
| 승인자 지정 | ✅ |
| 기본 결재선 자동 설정 | ⬚ |
| 결재선 수정 | ⬚ |
| 다단계 결재 | ⬚ |
| 참조 결재 | ⬚ |

---

### 3.5 근태신청 특화 로직

#### 3.5.1 잔여연차 검증 ⬚
- **목표 기능:**
  - 근태신청 시 연차 DB 조회
  - 잔여일 초과 신청 시 워닝 표시
  - 워닝 상태로 신청 가능 (완전 차단 아님)
  - 최종 결재 완료 시까지 워닝 유지
- **정책 검토 필요:**
  - 선사용(당겨쓰기) 허용 범위
  - 마이너스 연차 한도 설정

#### 3.5.2 휴일근무 처리 ✅
- 휴일근무 등록 시 대체휴무 자동 생성
- 휴일근무일 / 대체휴무일 연결

---

### 3.6 결재함 구성

| 구분 | 설명 | 상태 |
|------|------|------|
| 기안함 | 내가 상신한 문서 | 🔄 |
| 진행함(대기함) | 내가 결재해야 할 문서 | ✅ |
| 반려함 | 반려된 문서 | 🔄 |
| 완료함 | 승인 완료 문서 | 🔄 |
| 참조함 | 참조로 지정된 문서 | ⬚ |

- **관련 파일:** [my-approvals.html](src/main/resources/static/my-approvals.html)

---

### 3.7 알림 및 커뮤니케이션

#### 3.7.1 웹 알림 ✅
- 30초 폴링 방식
- 알림 벨 아이콘 (전 페이지 공통)
- 읽음/안읽음 상태 관리
- 알림 클릭 시 해당 페이지 이동

#### 3.7.2 외부 알림 연동 ⬚
- 이메일 알림 (SMTP)
- 네이버웍스 메시지 API
- 웹 푸시 (Web Push)

#### 3.7.3 코멘트 기능 ⬚
- 결재 문서 내 의견 기록
- 반려 사유 외 추가 코멘트

---

### 3.8 문서 관리

#### 3.8.1 문서 작성 ✅
- 문서 CRUD (생성/조회/수정/삭제)
- 파일 첨부 (원본 파일명 유지)
- 문서 승인 워크플로우

#### 3.8.2 문서 보관 ⬚
- 결재 완료 시 PDF 자동 생성
- 기간/작성자/유형별 검색
- Object Storage 연동

---

### 3.9 경비보고서

#### 3.9.1 경비 항목 관리 ✅
- 경비 항목 등록 (사용일, 계정, 금액)
- 복지비 여부 구분
- 월별 경비보고서 조회

#### 3.9.2 경비 신청 워크플로우 ✅
- EXPENSE_ITEMS_INTRANET (상세) → EXPENSE_ITEMS (마스터) 동기화
- 미확인 경비 알림 (EXPENSE_ITEM_READ_STATUS)
- 읽음 처리

#### 3.9.3 예산 관리 ⬚
- 부서별/프로젝트별 예산 설정
- 예산 대비 실적 현황
- 예산 초과 경고

---

### 3.10 권한 및 보안

#### 3.10.1 현재 권한 체계 ✅

| Role | 설명 |
|------|------|
| ADMIN | 관리자 (전체 접근) |
| USER | 일반 사용자 |

#### 3.10.2 목표 권한 체계 ⬚

| Role | 설명 |
|------|------|
| USER | 일반 사용자 |
| APPROVER | 결재자 (팀장/부서장) |
| ADMIN | 시스템 관리자 |
| PROXY | 대결자 |
| HR | 인사팀 (전사 조회) |

#### 3.10.3 대결자 지정 ⬚
- 출장/휴가 중 대결자 설정
- 대결 기간 지정
- 대결 결재 이력 기록

#### 3.10.4 감사 로그 ⬚
- 결재 및 수정 이력 로그
- 불변 저장 (Audit Trail)
- 로그 조회 (관리자)

---

## 4. 기술 명세 (Technical Specification)

### 4.1 현재 스택 (As-Is)

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.2.0, Java 17 |
| ORM | MyBatis 3.0 |
| Database | Oracle Database (Oracle XE) |
| Frontend | Vanilla JavaScript (SPA) |
| Calendar | FullCalendar 6.1.10 |
| Styling | Custom CSS + Bootstrap 5 |
| 인증 | Session 기반 |
| 빌드 | Maven |

### 4.2 목표 스택 (To-Be)

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.x, Java 21 |
| ORM | MyBatis 유지 |
| Database | Oracle Database (유지) |
| Cache | Redis |
| Frontend | React (Vite) |
| 인증 | OAuth2 + Naver Works (JWT) |
| 빌드 | Gradle |
| 배포 | Docker Container |
| Proxy | Nginx |
| CI/CD | GitHub Actions |

---

### 4.3 인프라 (To-Be)

| 항목 | 내용 |
|------|------|
| 접속 URL | `https://work.yncsmart.com` |
| OS | Rocky Linux 9.x |
| TLS | Let's Encrypt |
| 리버스 프록시 | Nginx(443) → Spring Boot(9090) |
| 파일 스토리지 | Object Storage (PDF 보관) |
| 백업 | DB 일/주기 백업 |

---

### 4.4 데이터베이스

#### 4.4.1 현재 테이블 (As-Is)

```
사용자/조직
├── MEMBERS_INTRANET         ✅ 사용자 정보
└── DEPARTMENTS_INTRANET     ✅ 부서 정보

일정/휴가
├── SCHEDULES_INTRANET       ✅ 일정/휴가 통합
└── SCHEDULE_APPROVALS_INTRANET ✅ 일정 승인

경비보고서
├── EXPENSE_REPORTS_INTRANET ✅ 경비보고서 헤더
├── EXPENSE_ITEMS_INTRANET   ✅ 경비 항목 (상세)
├── EXPENSE_ITEMS            ✅ 경비 마스터 (신청용)
└── EXPENSE_ITEM_READ_STATUS ✅ 경비 읽음 상태

문서
├── DOCUMENTS_INTRANET       ✅ 문서 정보
├── DOCUMENT_APPROVALS_INTRANET ✅ 문서 승인
└── DOCUMENT_FILES_INTRANET  ✅ 첨부파일

알림
└── NOTIFICATIONS_INTRANET   ✅ 알림 정보
```

#### 4.4.2 추가 예정 테이블 (To-Be)

```sql
-- 사용자 확장
users(id, external_id, name, dept, title, email, role, active)

-- 결재 양식
approval_forms(id, type, version, schema, active)

-- 결재 문서 통합
approval_docs(id, type, form_version, drafter_id, status, created_at, updated_at)
approval_lines(doc_id, seq, approver_id, type, state, acted_at, comment)

-- 연차 관리
leave_balances(user_id, year, total, used, remaining, max_advance)
leave_requests(doc_id, start_date, end_date, days, warning_flag)

-- 경비 확장
expense_requests(doc_id, amount, category, receipt_uri)

-- 자기개발비
devfund_balances(user_id, year, quota, used, remaining)

-- 감사 로그
audit_logs(id, actor, action, target, payload, created_at)

-- 대결자
proxy_settings(user_id, proxy_id, start_date, end_date, active)
```

---

### 4.5 보안/권한

| 항목 | 현재 | 목표 |
|------|------|------|
| 인증 | Session | JWT (Stateless) |
| 역할 | ADMIN/USER | USER/APPROVER/ADMIN/PROXY/HR |
| 입력검증 | 기본 | Bean Validation |
| 보안헤더 | 없음 | CSP, X-Frame-Options |
| Rate Limit | 없음 | 적용 예정 |

---

### 4.6 관측/운영 (To-Be)

| 항목 | 내용 |
|------|------|
| 로깅 | JSON 로그 + Trace ID |
| 메트릭 | Actuator + Prometheus/Grafana |
| 알람 | Error/Latency 기반 Slack/Email |
| 헬스체크 | `/actuator/health` |
| 피처플래그 | 신규 기능 안전 배포용 |

---

## 5. 개발 로드맵

### Phase 1: 현재 시스템 안정화 (현재)
- [x] 기본 인증 (Session)
- [x] 일정/휴가 캘린더
- [x] 기본 승인 워크플로우
- [x] 경비보고서
- [x] 알림 시스템
- [ ] 잔여연차 검증 (워닝) - 정책 검토 중
- [ ] 결재함 완성 (기안함/반려함/완료함)

### Phase 2: 기능 확장
- [ ] 인사팀 대시보드 (전사 근태/휴가 현황)
- [ ] 근태/휴가 통계 + 엑셀 다운로드
- [ ] 연차 자동 부여/이월 정책
- [ ] 일반기안 양식
- [ ] 권한 세분화 (APPROVER, HR, PROXY)

### Phase 3: 인프라 고도화
- [ ] 네이버웍스 OAuth 연동
- [ ] Redis 캐시 적용 (세션/알림)
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 (GitHub Actions)
- [ ] Nginx 리버스 프록시

### Phase 4: 프론트엔드 현대화 + 고급 기능
- [ ] React (Vite) 프론트엔드 전환
- [ ] PDF 자동 생성/보관
- [ ] 외부 알림 (이메일/웍스/푸시)
- [ ] 대결자 지정
- [ ] 감사 로그 (Audit Trail)

> **Note:** React 전환은 Phase 4에서 진행. 현재는 Vanilla JS 유지하며 기능 안정화 우선.

---

## 6. 변경 이력

| 버전 | 날짜 | 내용 |
|------|------|------|
| v1.0 | 2026-01-22 | 초안 작성 (As-Is + To-Be 통합) |

---

## 7. 관련 문서

- [README.md](README.md) - 프로젝트 개요
- [INTRANET_SETUP.md](INTRANET_SETUP.md) - 상세 개발 문서
- [Intranet_Dev_board.md](Intranet_Dev_board.md) - 개발 위원회 규칙
