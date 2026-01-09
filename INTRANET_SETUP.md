# YNC INTRANET 시스템 개발 가이드

## 📋 개요
YNC INTRANET은 기존 yncIntranet 프로젝트에 새로운 인트라넷 시스템을 추가한 프로젝트입니다.
기존 테이블(`members`, `events` 등)과 독립적으로 `_intranet` 접미사가 붙은 새 테이블로 구성됩니다.

---

## 🗂️ 프로젝트 구조

```
yncIntranet/
├── sql/                                    # 데이터베이스 스크립트
│   ├── 00_setup_guide.md                  # SQL 설치 가이드
│   ├── 01_create_tables.sql               # 테이블 생성 (12개)
│   ├── 02_create_sequences.sql            # 시퀀스 생성
│   ├── 03_create_indexes.sql              # 인덱스 생성
│   ├── 04_insert_common_data.sql          # 기본 데이터 삽입
│   ├── 99_drop_all.sql                    # 전체 삭제
│   └── check_installation.sql             # 설치 확인
│
├── src/main/java/com/ync/
│   ├── schedule/                          # 기존 시스템 (유지)
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── mapper/
│   │   ├── service/
│   │   └── controller/
│   │
│   └── intranet/                          # 새 인트라넷 시스템 ⭐
│       ├── domain/                        # Domain 클래스
│       │   ├── MemberIntranet.java
│       │   ├── DepartmentIntranet.java
│       │   ├── DocumentIntranet.java
│       │   ├── ApprovalLineIntranet.java
│       │   ├── LeaveRequestIntranet.java
│       │   ├── ExpenseReportIntranet.java
│       │   ├── ExpenseItemIntranet.java
│       │   ├── CommonCodeIntranet.java
│       │   └── NoticeIntranet.java
│       │
│       ├── dto/                           # DTO (추후 작성)
│       ├── mapper/                        # MyBatis Mapper 인터페이스
│       │   ├── MemberIntranetMapper.java
│       │   ├── DocumentIntranetMapper.java
│       │   └── ApprovalLineIntranetMapper.java
│       │
│       ├── service/                       # Service 계층 (추후 작성)
│       └── controller/                    # REST API (추후 작성)
│
└── src/main/resources/
    ├── application.yml                    # 설정 파일 (수정됨)
    └── mapper/
        ├── *.xml                          # 기존 Mapper XML
        └── intranet/                      # 인트라넷 Mapper XML ⭐
            ├── MemberIntranetMapper.xml
            ├── DocumentIntranetMapper.xml
            └── ApprovalLineIntranetMapper.xml
```

---

## 🗄️ 데이터베이스 구조

### 테이블 목록 (13개)

| 테이블명 | 설명 | 관계 |
|----------|------|------|
| **departments_intranet** | 부서 | - |
| **members_intranet** | 사원 (인증 포함) | → departments_intranet |
| **documents_intranet** | 문서 통합 ⭐ | → members_intranet |
| **schedules_intranet** | 일정/휴가 관리 🆕 | → members_intranet, documents_intranet |
| **leave_requests_intranet** | 휴가 신청 | → documents_intranet |
| **expense_reports_intranet** | 경비보고서 | → documents_intranet |
| **expense_items_intranet** | 경비 항목 | → expense_reports_intranet |
| **approval_lines_intranet** | 결재선 ⭐ | → documents_intranet |
| **attachments_intranet** | 첨부파일 | → documents_intranet |
| **notices_intranet** | 공지사항 | → members_intranet |
| **system_logs_intranet** | 시스템 로그 | → members_intranet |
| **common_codes_intranet** | 공통 코드 | - |
| **email_templates_intranet** | 메일 템플릿 | - |

### 핵심 설계 특징

#### 1. 문서 통합 구조 (documents_intranet)
```
모든 문서를 하나의 테이블로 통합 관리
- document_type: LEAVE, EXPENSE, GENERAL
- status: DRAFT, PENDING, APPROVED, REJECTED, CANCELED
- 상세 정보는 별도 테이블 (1:1)
```

#### 2. 결재선 스냅샷 (approval_lines_intranet)
```
결재 당시 정보를 저장하여 이력 보존
- approver_name: 결재 당시 이름
- approver_position: 결재 당시 직급
→ 나중에 직급이 바뀌어도 결재 이력 유지
```

#### 3. 권한 관리 (members_intranet.role)
```
- USER: 일반 사용자
- APPROVER: 결재권자
- ADMIN: 관리자
```

---

## 🚀 설치 방법

### 1단계: 데이터베이스 생성

```sql
-- Oracle SQL Developer 또는 SQL*Plus에서 실행

-- 1. 테이블 생성
@c:\smartWork\workspace\yncIntranet\sql\01_create_tables.sql

-- 2. 시퀀스 생성
@c:\smartWork\workspace\yncIntranet\sql\02_create_sequences.sql

-- 3. 인덱스 생성
@c:\smartWork\workspace\yncIntranet\sql\03_create_indexes.sql

-- 4. 기본 데이터 삽입
@c:\smartWork\workspace\yncIntranet\sql\04_insert_common_data.sql

-- 5. 일정/휴가 테이블 생성 🆕
@c:\smartWork\workspace\yncIntranet\sql\08_create_schedule_tables.sql

-- 6. 설치 확인
@c:\smartWork\workspace\yncIntranet\sql\check_installation.sql
```

### 2단계: 기본 데이터 확인

```sql
-- 관리자 계정 확인
SELECT email, name, role FROM members_intranet;
-- 결과: admin@yncsmart.com / 시스템관리자 / ADMIN

-- 공통 코드 확인
SELECT code_type, COUNT(*) FROM common_codes_intranet GROUP BY code_type;
-- 결과: LEAVE_TYPE(7), EXPENSE_CATEGORY(7), POSITION(9)

-- 부서 확인
SELECT * FROM departments_intranet;
-- 결과: 경영지원팀, 개발팀, 영업팀
```

### 3단계: 애플리케이션 설정 확인

[application.yml](src/main/resources/application.yml) 파일이 자동으로 업데이트 되었습니다:

```yaml
mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.ync.schedule.domain,com.ync.intranet.domain

logging:
  level:
    com.ync.intranet: DEBUG
```

---

## 📝 개발 완료 현황

### ✅ 완료된 작업

1. **데이터베이스 스키마** (100%)
   - 테이블 12개 생성
   - 시퀀스 11개 생성
   - 인덱스 50개+ 생성
   - 기본 데이터 삽입

2. **Domain 클래스** (100%)
   - MemberIntranet.java
   - DepartmentIntranet.java
   - DocumentIntranet.java
   - ApprovalLineIntranet.java
   - LeaveRequestIntranet.java
   - ExpenseReportIntranet.java
   - ExpenseItemIntranet.java
   - CommonCodeIntranet.java
   - NoticeIntranet.java

3. **Mapper 인터페이스** (50%)
   - MemberIntranetMapper.java ✅
   - DocumentIntranetMapper.java ✅
   - ApprovalLineIntranetMapper.java ✅
   - DepartmentIntranetMapper.java ⏸️
   - LeaveRequestIntranetMapper.java ⏸️
   - ExpenseReportIntranetMapper.java ⏸️

4. **Mapper XML** (50%)
   - MemberIntranetMapper.xml ✅
   - DocumentIntranetMapper.xml ✅
   - ApprovalLineIntranetMapper.xml ✅

5. **Service 계층** (100%) ✅
   - AuthService (로그인/로그아웃) ✅
   - MemberIntranetService ✅
   - DocumentIntranetService ✅
   - ApprovalService ✅

6. **Controller** (100%) ✅
   - AuthController ✅
   - MemberIntranetController ✅
   - ApprovalController ✅

7. **인증 시스템** (100%) ✅
   - Session 기반 인증 ✅
   - BCrypt 비밀번호 암호화 ✅
   - Session 기반 권한 체크 ✅

### ⏳ 추가 개발 가능 항목

8. **나머지 Controller** (0%)
   - DocumentController (문서 작성/상신)
   - LeaveRequestController (휴가 신청)
   - ExpenseReportController (경비보고서)
   - DashboardController (대시보드)
   - NoticeController (공지사항)

---

## 🔐 기본 계정

```
이메일: admin@yncsmart.com
비밀번호: admin1234
권한: ADMIN
```

> ⚠️ **보안 주의:** 최초 로그인 후 반드시 비밀번호를 변경하세요!

---

## 📊 Domain 클래스 상세

### MemberIntranet (사원)
```java
- id: Long
- email: String (로그인 ID)
- password: String (BCrypt 암호화)
- name: String
- departmentId: Long
- position: String (직급)
- role: String (USER, APPROVER, ADMIN)
- hireDate: LocalDate
- annualLeaveGranted: BigDecimal
- isActive: Boolean
```

### DocumentIntranet (문서 통합)
```java
- id: Long
- documentType: DocumentType (LEAVE, EXPENSE, GENERAL)
- authorId: Long
- title: String
- content: String
- status: DocumentStatus (DRAFT, PENDING, APPROVED, REJECTED)
- metadata: String (JSON)
- submittedAt: LocalDateTime
- approvedAt: LocalDateTime
```

### ApprovalLineIntranet (결재선)
```java
- id: Long
- documentId: Long
- stepOrder: Integer (1, 2, 3...)
- approverId: Long
- approverName: String (스냅샷)
- approverPosition: String (스냅샷)
- decision: ApprovalDecision (PENDING, APPROVED, REJECTED)
- approvalComment: String
- decidedAt: LocalDateTime
```

---

## 🎯 개발 가이드

### Mapper 작성 예시

```java
@Mapper
public interface ExampleMapper {
    Example findById(@Param("id") Long id);
    void insert(Example example);
}
```

```xml
<mapper namespace="com.ync.intranet.mapper.ExampleMapper">
    <select id="findById" resultType="com.ync.intranet.domain.Example">
        SELECT * FROM example_table WHERE id = #{id}
    </select>
</mapper>
```

### Service 작성 예시

```java
@Service
public class ExampleService {
    private final ExampleMapper exampleMapper;

    public ExampleService(ExampleMapper exampleMapper) {
        this.exampleMapper = exampleMapper;
    }

    public Example findById(Long id) {
        return exampleMapper.findById(id);
    }
}
```

### Controller 작성 예시

```java
@RestController
@RequestMapping("/api/intranet/example")
@CrossOrigin(origins = "*")
public class ExampleController {
    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Example> getExample(@PathVariable Long id) {
        return ResponseEntity.ok(exampleService.findById(id));
    }
}
```

---

## 🛠️ 트러블슈팅

### Q: "comment cannot be resolved" 오류
→ Oracle 예약어 충돌. `approval_comment`로 변경 완료

### Q: Mapper XML을 찾을 수 없음
→ application.yml에서 `mapper-locations: classpath*:mapper/**/*.xml` 확인

### Q: Domain 클래스 타입 인식 안됨
→ application.yml에서 `type-aliases-package`에 패키지 추가 확인

### Q: 일정이 캘린더에 표시되지 않음
→ **원인**: 회의/출장 일정이 DRAFT 상태로 저장되었으나 캘린더는 APPROVED 상태만 표시
→ **해결**: ScheduleIntranetService.java에서 MEETING/BUSINESS_TRIP 타입은 자동으로 APPROVED 상태로 저장하도록 수정 (v0.6)

### Q: 일정 수정 시 "수정에 실패하였습니다: null" 오류 및 중복 생성
→ **원인 1**: ScheduleIntranetMapper.xml의 UPDATE 쿼리에 `approver_id`, `document_id` 필드 누락
→ **원인 2**: 프론트엔드에서 기존 일정 데이터를 보존하지 않고 항상 DRAFT 상태로 전송
→ **해결**:
  - Mapper XML에 누락된 필드 추가
  - schedule-calendar.html에 `currentEditingSchedule` 전역 변수 추가하여 기존 데이터 보존 (v0.6)

### Q: 휴가 현황 계산이 정확하지 않음
→ 휴가 현황은 APPROVED 상태 일정만 집계하도록 구현되어 있음 (정상 작동)
→ 일정이 표시되지 않는 경우 위의 "일정이 캘린더에 표시되지 않음" 문제 확인

---

## 📞 현재 상태 및 사용 방법

### ✅ 완성된 기능 (바로 사용 가능)

1. **로그인/로그아웃** - Session 기반 인증 완료
2. **사원 관리** - 조회, 등록, 수정, 비활성화
3. **결재 시스템** - 승인, 반려, 취소
4. **일정/휴가 관리** - 캘린더 기반 일정 관리 및 결재 연동 (v0.4)
5. **지출보고서 관리** - 지출 내역 관리 및 엑셀 다운로드, 복지비 자동 태깅 (v0.5) 🆕

### 🚀 실행 방법

```bash
# 1. 데이터베이스 설정 완료 확인
# sql 폴더의 스크립트 실행 완료 확인

# 2. 애플리케이션 실행
mvn clean install
mvn spring-boot:run

# 3. API 테스트
# http://localhost:8083 으로 접속
```

### 📖 API 문서

자세한 API 사용법은 [API_GUIDE.md](API_GUIDE.md) 참조

---

## 📅 일정/휴가 관리 시스템

### 개요
일정 및 휴가를 캘린더 형식으로 관리하는 시스템입니다. FullCalendar 라이브러리를 사용하여 직관적인 UI를 제공합니다.

### 주요 기능

#### 1. 캘린더 기반 일정 관리
- **FullCalendar 6.1.10** 사용
- 월간/주간/목록 뷰 지원
- 드래그 앤 드롭으로 날짜 선택
- 일정 클릭 시 상세 정보 표시

#### 2. 일정 유형
- **연차 (VACATION)**: 1일 이상의 휴가
- **반차 (HALF_DAY)**: 오전반차 (09:00-13:00) / 오후반차 (13:00-18:00)
- **출장 (BUSINESS_TRIP)**: 출장 일정
- **회의 (MEETING)**: 회의 일정 (시간 지정 가능)

#### 3. 일정 등록 기능
- **자동 날짜 매핑**: 시작일 선택 시 종료일 자동 설정
- **시간 입력**: 회의/출장 시 시작/종료 시간 입력
- **반차 구분**: 오전/오후 자동 시간 설정
- **사용 일수 자동 계산**: 주말 제외 자동 계산

#### 4. 사이드바 기능
- **휴가 현황**: 부여/사용/잔여 일수 표시
- **결재 대기**: 승인 대기 중인 일정 목록 및 승인/반려 처리
- **내 일정**: 등록된 내 일정 목록 (시간 정보 포함)
- **필터링**: 부서/구성원별 필터링

#### 5. 결재 연동
- 일정 저장 시 자동 결재 요청
- `documents_intranet`와 연동하여 결재 처리
- 사이드바에서 바로 승인/반려 가능

### 데이터베이스 스키마

#### schedules_intranet 테이블
```sql
CREATE TABLE schedules_intranet (
    id NUMBER PRIMARY KEY,
    member_id NUMBER NOT NULL,                -- 작성자 (FK: members_intranet)
    schedule_type VARCHAR2(50) NOT NULL,      -- VACATION, HALF_DAY, BUSINESS_TRIP, MEETING
    title VARCHAR2(200) NOT NULL,             -- 제목
    description CLOB,                          -- 설명
    start_date DATE NOT NULL,                  -- 시작일
    end_date DATE NOT NULL,                    -- 종료일
    start_time VARCHAR2(5),                    -- 시작 시간 (HH:MI)
    end_time VARCHAR2(5),                      -- 종료 시간 (HH:MI)
    days_used NUMBER(3,1) DEFAULT 0,           -- 사용 일수 (0.5, 1, 1.5 등)
    document_id NUMBER,                        -- 연결된 결재 문서 ID (FK: documents_intranet)
    status VARCHAR2(20) DEFAULT 'DRAFT',       -- DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_member FOREIGN KEY (member_id) REFERENCES members_intranet(id),
    CONSTRAINT fk_schedule_document FOREIGN KEY (document_id) REFERENCES documents_intranet(id) ON DELETE SET NULL
);
```

### 프론트엔드 페이지

#### schedule-calendar.html
- **위치**: `src/main/resources/static/schedule-calendar.html`
- **접근**: 메인 화면 → "일정/휴가 관리" 카드 클릭
- **기능**:
  - FullCalendar 기반 캘린더 뷰
  - 일정 추가/수정 모달
  - 사이드바 (필터, 휴가현황, 결재대기, 내일정)
  - 부서/구성원 필터링
  - 승인/반려 처리

### API 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/intranet/schedules` | 일정 등록 및 결재 요청 |
| GET | `/api/intranet/schedules` | 일정 목록 조회 |
| GET | `/api/intranet/schedules/{id}` | 일정 상세 조회 |
| PUT | `/api/intranet/schedules/{id}` | 일정 수정 |
| DELETE | `/api/intranet/schedules/{id}` | 일정 삭제 |
| GET | `/api/intranet/schedules/calendar` | 캘린더용 일정 조회 (필터 지원) |

### 사용 흐름

1. **일정 등록**
   - 캘린더에서 날짜 선택 또는 "새 일정" 버튼 클릭
   - 일정 유형 선택 (연차/반차/출장/회의)
   - 제목, 날짜, 시간(선택적) 입력
   - 저장 시 자동으로 결재 요청됨

2. **결재 처리**
   - 결재권자가 사이드바의 "결재 대기" 목록 확인
   - 승인/반려 버튼 클릭하여 즉시 처리
   - 승인 시 일정이 캘린더에 표시됨

3. **일정 조회**
   - 캘린더에서 월/주/목록 뷰로 전환
   - 사이드바에서 부서/구성원 필터링
   - 일정 클릭 시 상세 정보 팝업

---

## 💰 지출보고서 관리 시스템

### 개요
법인카드 및 개인 경비 지출 내역을 관리하고 엑셀로 다운로드할 수 있는 시스템입니다.

### 주요 기능

#### 1. 지출 내역 관리
- **지출 추가 모달**: 개별 지출 항목 등록
- **행추가 기능**: 여러 항목을 한번에 대량 입력
- **필터링**: 사용자, 부서, 날짜, 복지비 여부로 필터링
- **엑셀 다운로드**: 선택한 조건으로 엑셀 파일 생성

#### 2. 복지비 자동 태깅 (v0.5 신규 기능) 🆕
- **자동 태그 추가**: 복지비 체크박스 선택 시 메모에 "[복지비]" 자동 추가
- **자동 태그 제거**: 체크 해제 시 "[복지비]" 자동 제거
- **지출 추가 모달 연동**: 팝업에서 복지비 체크 시 실시간 반영
- **행추가 연동**: 대량 입력 시에도 복지비 체크 가능
- **기존 데이터 로딩**: 복지비 플래그가 'Y'인 항목은 자동으로 "[복지비]" 표시

#### 3. 지출 항목 필드
- **사용일시**: 지출이 발생한 날짜
- **사용 내용**: 지출 설명 (예: 회의비, 교통비)
- **계정**: 지출 계정 (예: 복리후생비, 교통비)
- **사용금액**: 지출 금액
- **업소명**: 사용한 업체명
- **경비코드**: 경비 분류 코드
- **프로젝트코드**: 연결된 프로젝트 코드
- **비고**: 추가 메모 (복지비 태그 포함)
- **복지비 여부**: Y/N 플래그

#### 4. 엑셀 다운로드 기능
- **템플릿 기반**: expense_report_template.xlsx 템플릿 사용
- **동적 시트명**: "지출내역(매니저명)", "지출보고서(매니저명)"
- **자동 서식**: 템플릿의 스타일 및 수식 유지
- **증빙서류 카운트**: 총 라인 수 자동 계산

### 데이터베이스 스키마

#### expense_items_intranet 테이블
```sql
CREATE TABLE expense_items_intranet (
    id NUMBER PRIMARY KEY,
    member_id NUMBER,                          -- 사용자 ID (FK: members_intranet)
    usage_date DATE,                           -- 사용일시
    description VARCHAR2(500),                 -- 사용 내용
    account VARCHAR2(100),                     -- 계정
    amount NUMBER,                             -- 사용금액
    vendor VARCHAR2(200),                      -- 업소명
    cost_code VARCHAR2(100),                   -- 경비코드
    project_code VARCHAR2(100),                -- 프로젝트코드
    note CLOB,                                 -- 비고 (복지비 태그 포함)
    welfare_flag VARCHAR2(1) DEFAULT 'N',     -- 복지비 여부 (Y/N)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 프론트엔드 페이지

#### expense-report_intranet.html
- **위치**: `src/main/resources/static/expense-report_intranet.html`
- **접근**: 메인 화면 → "지출보고서 관리" 카드 클릭
- **기능**:
  - 지출 내역 테이블 표시
  - 필터링 (사용자, 부서, 날짜, 복지비)
  - 지출 추가 모달
  - 행추가 대량 입력
  - 엑셀 다운로드

### 데이터베이스 마이그레이션 스크립트

#### update-welfare-note.sql
- **위치**: `database/update-welfare-note.sql`
- **목적**: 기존 데이터 중 welfare_flag='Y'이지만 메모에 "[복지비]"가 없는 항목 업데이트
- **실행**:
  ```bash
  sqlplus64 username/password@xe @database/update-welfare-note.sql
  ```
- **기능**:
  - 메모가 NULL인 경우: "[복지비] " 설정
  - 메모가 있지만 "[복지비]"로 시작하지 않는 경우: "[복지비] " 접두어 추가
  - updated_at 컬럼 자동 업데이트

### 사용 흐름

1. **지출 추가 (모달)**
   - "지출 추가" 버튼 클릭
   - 지출 정보 입력
   - 복지비 체크 시 메모에 "[복지비]" 자동 추가
   - 저장

2. **지출 대량 입력 (행추가)**
   - "행추가" 버튼 클릭
   - 테이블에 여러 행 추가
   - 각 행에서 복지비 체크박스 선택 가능
   - 체크 시 해당 행의 메모에 "[복지비]" 자동 추가
   - "저장" 버튼으로 일괄 저장

3. **엑셀 다운로드**
   - 필터 조건 선택 (사용자, 부서, 날짜)
   - "엑셀 다운로드" 버튼 클릭
   - 템플릿 기반 엑셀 파일 생성 및 다운로드

---

## 📅 버전 히스토리

- **v0.16.1** (2026-01-09) - ScheduleIntranetService 메서드명 오류 수정 🆕
  - **DocumentIntranetMapper 메서드 호출 오류 수정**:
    - 기존 문제: withdrawCancellation() 메서드에서 존재하지 않는 메서드 호출로 컴파일 에러 발생
    - 원인: DocumentIntranetMapper에 findAll(), delete(Long) 메서드 없음
    - 수정 내용:
      - `documentMapper.findAll()` → `documentMapper.findAllOrderByCreatedAtDesc()`
      - `documentMapper.delete(cancelDoc.getId())` → `documentMapper.deleteById(cancelDoc.getId())`
    - ScheduleIntranetService.java lines 320, 343

- **v0.16** (2026-01-09) - 완료 문서함 날짜 범위 동적 설정
  - **현재 월 기준 날짜 범위 자동 설정**:
    - 기존 문제: 완료 문서함 날짜 범위가 1/1~1/8로 하드코딩됨
    - 요구사항: 시작일 = 현재 월의 1일, 종료일 = 현재 월의 말일
    - 개선: setDefaultDateRange() 함수 수정
      - `new Date(year, month, 1)` - 현재 월의 1일
      - `new Date(year, month + 1, 0)` - 현재 월의 말일
      - formatDateToYYYYMMDD() 함수 추가 (로컬 시간 기준 포맷)
    - 타임존 이슈 해결: toISOString() 대신 로컬 시간 기준 포맷 사용
    - UTC 변환으로 인한 날짜 오차 방지 (KST는 UTC+9)
    - 매월 동적으로 날짜 범위가 자동 계산됨
    - approval-pending.html lines 1043-1066

- **v0.15** (2026-01-08) - 취소 신청 반려 시 상태 복원 로직 추가
  - **취소 신청 반려 시 원본 일정 APPROVED 복원**:
    - 기존 문제: 취소 신청 반려 시 원본 일정이 REJECTED로 변경됨
    - 요구사항: 취소 신청이 반려되면 원본 일정은 APPROVED 상태로 복원되어야 함
    - 개선: reject() 메서드에 취소 문서 여부 확인 로직 추가
      - 취소 문서 반려: 원본 일정 → APPROVED 복원
      - 일반 문서 반려: 연결된 일정 → REJECTED
    - restoreCancellationRejection() 메서드 추가
      - syncCancellationStatus()와 동일한 패턴 사용
      - metadata에서 originalScheduleId 추출
      - 원본 일정 상태를 APPROVED로 복원
    - ApprovalService.java lines 155-164, 259-286

- **v0.14.1** (2026-01-08) - 일정 상태 제어 로직 버그 수정
  - **REJECTED 상태 읽기 전용 처리**:
    - 기존 문제: v0.14에서 REJECTED 상태를 수정 가능하도록 설정하여 요구사항 불일치
    - 요구사항: REJECTED는 읽기 전용, 삭제만 가능 (CANCELLED, COMPLETED와 동일)
    - 개선: REJECTED 상태를 완전 읽기 전용으로 변경
    - canEdit: true → false, isReadOnly: false → true
    - canDelete: true 유지 (삭제는 허용)
    - schedule-calendar.html lines 1480-1482, 1498-1500

  - **내 일정 선택 후 캘린더 비활성화 문제 완전 해결**:
    - 기존 문제: v0.14에서 초기화 로직 위치 오류로 버튼 상태가 이전 일정으로 고정됨
    - 근본 원인: 필드 초기화는 했으나 버튼 초기화를 누락
    - 개선: showEventDetail() 함수 시작 시 완전한 초기화 로직 추가
      1. 모달 열기
      2. 모든 필드 활성화 (disabled = false)
      3. 모든 버튼 표시 (display = inline-block)
      4. 폼 데이터 채우기
      5. 액션 결정 (determineAvailableActions)
      6. 새로운 일정의 상태에 따라 필드/버튼 재설정
    - 캘린더 정상 동작 보장: 초기화 대상은 모달 내부 요소만, 캘린더 자체는 절대 비활성화 안 됨
    - schedule-calendar.html lines 1434-1448, 1534-1537

- **v0.14** (2026-01-08) - 일정 상태 제어 로직 개선 (결함 있음, v0.14.1에서 수정)
  - **내 일정 선택 후 캘린더 상태 초기화** (불완전):
    - 기존 문제: 내 일정에서 읽기 전용 일정(APPROVED/CANCELLED) 선택 후 캘린더에서 다른 일정 클릭 시 이전 상태가 유지되어 비활성화됨
    - 개선 시도: showEventDetail() 함수에서 필드 초기화 추가
    - 결함: 버튼 초기화 누락, 초기화 로직 위치 오류로 form 변수 중복 선언
    - ⚠️ v0.14.1에서 완전히 수정됨

  - **REJECTED 상태 버튼 제어 로직 명확화** (요구사항 불일치):
    - 개선 시도: canSubmit 플래그 추가로 저장과 결재 신청 분리
    - 결함: REJECTED를 수정 가능(canEdit: true)으로 설정하여 요구사항과 불일치
    - ⚠️ v0.14.1에서 읽기 전용으로 수정됨

- **v0.13.1** (2026-01-08) - 취소 신청 tooltip 표시 및 지출보고서 링크 개선
  - **취소 신청 tooltip 상태 표시 개선**:
    - 기존: 캘린더 이벤트 제목에만 "(취소 신청 중)" 표시, tooltip은 "결재 대기" 표시
    - 개선: tooltip에도 "취소 대기중" 상태 표시 추가
    - isCancellationRequest() 로직을 tooltip 렌더링에도 적용
    - documentTitle을 extendedProps에 추가하여 tooltip에서도 취소 신청 여부 감지
    - schedule-calendar.html lines 941-967 (tooltip), line 1171 (extendedProps)

  - **새 일정 작성 시 캘린더 활성화 보장**:
    - 기존: 결재 항목 선택 후 새 일정 작성 시 캘린더가 비활성화되는 버그
    - 개선: openEventModal() 함수에 currentEditingSchedule = null 초기화 추가
    - 새 일정 작성과 기존 일정 수정을 명확히 구분
    - schedule-calendar.html line 1319

  - **지출보고서 페이지 일정관리 버튼 링크 수정**:
    - 기존: schedule.html로 이동 (구 캘린더)
    - 개선: schedule-calendar.html로 이동 (신규 캘린더)
    - expense-report_intranet.html line 1334
    - expense-report.html line 1118

- **v0.13** (2026-01-08) - 취소 신청 UX 개선 및 완료 일정 제한
  - **취소 신청 상태 표시 개선 (캘린더)**:
    - 백엔드 상태값: PENDING 유지
    - UI 표시: "취소 신청 중" 텍스트 추가
    - isCancellationRequest() 함수로 취소 신청 여부 판단
    - 캘린더 이벤트 제목에 "(취소 신청 중)" 표시
    - schedule-calendar.html lines 1254-1286

  - **내 일정 취소 신청 표시 및 철회 기능**:
    - 취소 신청 중인 일정: 상태 배지 "취소 신청 중" (주황색)
    - "철회" 버튼 추가: 취소 신청을 철회하고 승인 상태로 복원
    - withdrawCancellation() API 구현 (프론트엔드 + 백엔드)
    - 철회 시 취소 문서 및 결재선 삭제, 일정 상태 APPROVED로 복원
    - UI 즉시 갱신: loadMySchedules() + calendar.refetchEvents()
    - schedule-calendar.html lines 2319-2358, 1733-1758
    - ScheduleIntranetService.java lines 297-348
    - ScheduleIntranetController.java lines 260-293

  - **회의/출장 COMPLETED 상태 수정 제한**:
    - 기존: CANCELLED만 수정 불가
    - 개선: CANCELLED, COMPLETED 모두 수정 불가
    - 완료된 회의/출장은 읽기 전용으로 변경
    - 프론트엔드: determineAvailableActions() 함수 수정
    - 백엔드: updateSchedule() 검증 로직 추가
    - schedule-calendar.html lines 1476-1485
    - ScheduleIntranetService.java lines 124-129

- **v0.12** (2026-01-08) - 일정 타입별 상태 관리 개선
  - **일정 타입별 차별화된 액션 로직**:
    - 기존 문제: v0.11에서 CANCELLED/REJECTED/APPROVED를 동일하게 처리하여 회의/출장 일정이 APPROVED 상태일 때 수정 불가한 버그 발생
    - 개선: 일정 타입(연차/반차 vs 회의/출장)에 따라 다른 액션 규칙 적용
    - `determineAvailableActions()` 함수 도입으로 상태별 가능한 액션 명확히 정의

  - **연차/반차 액션 규칙**:
    - DRAFT/REJECTED: 수정 + 삭제 가능
    - SUBMITTED/PENDING: 읽기 전용 (결재 진행 중)
    - APPROVED: 읽기 전용 + 취소 신청 버튼만 표시
    - CANCELLED: 완전 읽기 전용

  - **회의/출장 액션 규칙**:
    - CANCELLED: 읽기 전용
    - 그 외 모든 상태: 수정 + 삭제 가능 (APPROVED 포함)

  - **백엔드 검증 로직 개선**:
    - ScheduleIntranetService.updateSchedule() 메서드 수정
    - 일정 타입별로 다른 상태 검증 규칙 적용
    - 연차/반차: DRAFT, REJECTED 상태만 수정 가능
    - 회의/출장: CANCELLED 제외하고 항상 수정 가능
    - schedule-calendar.html lines 1426-1504
    - ScheduleIntranetService.java lines 103-133

- **v0.11** (2026-01-08) - 일정 상태 관리 및 UI 개선
  - **일정 상태별 수정 제한**:
    - CANCELLED, REJECTED, APPROVED 상태 일정은 수정 불가
    - 프론트엔드: 폼 필드 disabled 처리, 버튼 숨김
    - 모달 제목: "일정 상세 (읽기 전용)" 표시
    - 백엔드: ScheduleIntranetService.updateSchedule()에 상태 검증 로직 추가
    - IllegalStateException 발생으로 수정 차단

  - **모든 상태 아이콘 통일**:
    - 기존: PENDING, IN_PROGRESS만 아이콘 표시
    - 개선: 모든 상태에 아이콘 적용
    - 📝 DRAFT, ⏳ SUBMITTED/PENDING, ✅ APPROVED, ❌ REJECTED, 🚫 CANCELLED, 📅 RESERVED, ▶ IN_PROGRESS, ✔ COMPLETED
    - schedule-calendar.html lines 1240-1250

  - **캘린더 뷰 버튼 활성화 표시**:
    - 월/주/목록 버튼 선택 시 시각적 강조
    - 활성 버튼: 보라색 배경 (#667eea), 흰색 텍스트, 굵은 글씨
    - 비활성 버튼: 흰색 배경, 회색 텍스트
    - CSS .fc-button-active 스타일 수정

- **v0.10** (2026-01-08) - 캘린더 및 취소 상태 UI/UX 개선
  - **PENDING 연차/반차 캘린더 표시**:
    - 결재 대기 중인 연차/반차도 캘린더에 표시
    - 상태별 시각적 구분: PENDING/SUBMITTED (50% 투명도), IN_PROGRESS (노란색 테두리)
    - 상태 아이콘 추가: ⏳ (결재 대기), ▶ (진행 중)

  - **캘린더 이벤트 제목 통일**:
    - 모든 일정 제목을 "[일정 타입] [사용자명]" 형식으로 통일
    - 예: "연차 홍길동", "회의 김철수", "출장 이영희"
    - schedule-calendar.html lines 1232-1251 수정

  - **일정 수정 시 제목 자동 매핑**:
    - 일정 유형 선택 시 제목 자동 설정
    - handleEventTypeChange() 함수에 자동 매핑 로직 추가
    - schedule-calendar.html lines 1718-1728

  - **CANCELLED 상태 동기화 문제 해결**:
    - 취소 승인 후 상태가 APPROVED로 되돌아가는 버그 수정
    - getScheduleById() 메서드에 CANCELLED 상태 보호 로직 추가
    - 취소된 일정은 원본 문서 상태와 동기화하지 않음
    - ScheduleIntranetService.java lines 131-135

  - **취소 요청 버튼 숨김 로직 강화**:
    - CANCELLED 상태 일정은 "취소 신청" 버튼 표시 안 함
    - 프론트엔드 검증 로직 이미 구현되어 있음 (lines 1445-1448)
    - 백엔드 검증 로직 이미 구현되어 있음 (lines 207-215)

  - **상태 일관성 보장**:
    - Calendar View: 필터링된 상태만 표시
    - My Schedule View: 실시간 상태 반영
    - Detail View: DB 상태와 동기화
    - CANCELLED 상태는 영구 보존

- **v0.9** (2026-01-08) - 연차/반차 취소 신청 워크플로우
  - **취소 신청 기능 구현 (v0.9.1)**:
    - requestCancellation() 메서드 구현
    - 승인된 연차/반차에 대한 취소 신청 가능
    - 취소 문서 자동 생성 (제목: "[취소] 원본 제목")
    - metadata에 originalScheduleId 저장하여 원본 일정 추적
    - 취소 결재선 생성 (원본 결재자와 동일)
    - 일정 상태를 PENDING으로 변경
    - ScheduleIntranetService.java lines 221-295
    - ScheduleIntranetController.java POST /api/intranet/schedules/{id}/cancel

  - **취소 승인 시 상태 동기화**:
    - ApprovalService.syncCancellationStatus() 메서드 추가
    - 취소 문서 승인 시 원본 일정을 CANCELLED로 변경
    - 문서 제목이 "[취소]"로 시작하는지 확인
    - metadata에서 originalScheduleId 추출하여 원본 일정 업데이트
    - ApprovalService.java lines 222-253

  - **회의/출장 중복 생성 버그 수정 (v0.9.2)**:
    - 일정 수정 시 새 일정이 생성되는 버그 수정
    - currentEditingSchedule 존재 시 PUT 요청으로 라우팅
    - 폼 제출 로직에 수정/생성 구분 추가
    - schedule-calendar.html 폼 제출 핸들러 수정

- **v0.8** (2026-01-07) - 결재 시스템 개선
  - **결재 대기 목록 조회 기능**:
    - 결재자별 대기중인 결재 목록 API
    - 완료된 결재 목록 조회 (제목/기간 검색 가능)
    - ApprovalService 메서드 추가

  - **결재 상태 표시 개선**:
    - 일정 상세 화면에 결재 상태 섹션 추가
    - 결재선 정보 표시 (결재자, 상태, 날짜)
    - PENDING/APPROVED/REJECTED 상태별 시각적 구분

- **v0.7** (2026-01-07) - 회의/출장 일정 실시간 상태 관리 시스템
  - **STATUS 값 확장**:
    - 회의/출장 전용 상태 추가: RESERVED (예약됨), IN_PROGRESS (진행중), COMPLETED (완료됨)
    - 기존 STATUS: DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED
    - sql/13_alter_schedules_status.sql 마이그레이션 스크립트 생성

  - **시간 기반 자동 상태 업데이트**:
    - ScheduleIntranetService에 calculateMeetingStatus() 메서드 추가
    - 한국 표준시(KST) 기준으로 현재 시간과 일정 시간 비교
    - START_TIME, END_TIME 컬럼 활용하여 실시간 상태 판정

  - **배치 작업 구현**:
    - ScheduleStatusUpdateTask 클래스 생성
    - 매 5분마다 회의/출장 일정 상태 자동 업데이트
    - 매일 자정 전체 일정 재계산
    - @EnableScheduling 활성화

  - **결재 승인 후 상태 동기화**:
    - ApprovalService에 syncScheduleStatus() 메서드 추가
    - 결재 승인/반려/취소 시 연결된 일정 상태 자동 업데이트
    - 연차/반차 결재 워크플로우와 일정 상태 완전 동기화

  - **문서화**:
    - SCHEDULE_STATUS_UPDATE_v0.7.md 상세 가이드 생성
    - 데이터 흐름, 상태 다이어그램, 트러블슈팅 가이드 포함

- **v0.6** (2026-01-07) - 일정/휴가 관리 핵심 버그 수정
  - **일정 표시 버그 수정**:
    - 회의/출장 일정이 화면에 표시되지 않던 문제 해결
    - ScheduleIntranetService.java 수정: MEETING/BUSINESS_TRIP 타입은 자동으로 APPROVED 상태로 저장 (결재 불필요)
    - 캘린더는 APPROVED 상태 일정만 표시하므로 이제 정상 표시됨

  - **일정 수정 버그 수정**:
    - 일정 수정 시 "수정에 실패하였습니다: null" 오류 발생 및 중복 생성 문제 해결
    - ScheduleIntranetMapper.xml UPDATE 쿼리에 누락된 필드 추가: `approver_id`, `document_id`
    - schedule-calendar.html 수정: 전역 변수 `currentEditingSchedule` 추가하여 기존 일정 데이터 보존
    - 일정 수정 시 memberId, approverId, documentId, status 등 기존 값 유지
    - 모달 닫을 때 currentEditingSchedule 초기화

  - **레이아웃 개선**:
    - "내 일정" 섹션을 오른쪽 사이드바에서 왼쪽 사이드바로 이동
    - "결재 대기" 아래 배치하여 레이아웃 균형 개선
    - 양쪽 사이드바 섹션에 스크롤 기능 추가 (max-height: 320px)
    - 캘린더가 전체 너비 사용 가능하도록 개선

  - **캘린더 UI 개선**:
    - 폰트 크기 증가: 기본 14px, 제목 24px
    - 날짜 셀 높이 증가: 100px (텍스트 오버플로우 방지)
    - 이벤트 제목 텍스트 줄바꿈 처리
    - 시간 정보 표시 개선

  - **결재 시스템 개선**:
    - approval-pending.html에 탭 기능 추가: "결재 대기" / "완료 문서함"
    - 완료 문서함에서 제목, 날짜 검색 기능 추가
    - 페이지네이션 기능 추가 (페이지당 5건)
    - 부서 정보 표시 (ApprovalLineIntranetMapper.xml에 JOIN 추가)

  - **휴가 현황 계산**:
    - 기존 로직 검증 완료 (APPROVED 상태만 집계)
    - 정상 작동 확인

  - **데이터베이스 유틸리티**:
    - sql/delete_member_신의섭.sql 스크립트 생성
    - 특정 사용자의 결재선, 일정, 문서 데이터 일괄 삭제 가능

- **v0.5** (2026-01-04) - 지출보고서 복지비 자동 태깅 기능 추가
  - expense-report_intranet.html에 복지비 체크박스 자동 태깅 기능 구현
  - 복지비 체크 시 메모 필드에 "[복지비]" 자동 추가/제거
  - "지출 추가" 팝업 모달에서 복지비 체크박스 연동
  - "행추가" 대량 입력에서 복지비 체크박스 연동
  - 기존 데이터 로딩 시 복지비 플래그에 따라 "[복지비]" 자동 표시
  - update-welfare-note.sql 스크립트 추가 (기존 DB 레코드 업데이트)

- **v0.4** (2026-01-01) - 일정/휴가 관리 시스템 추가
  - schedule-calendar.html 페이지 추가
  - schedules_intranet 테이블 추가
  - FullCalendar 6.1.10 통합
  - 일정 유형별 처리 (연차/반차/출장/회의)
  - 시간 입력 및 반차 오전/오후 구분
  - 사이드바 필터링 및 결재 처리
  - 날짜 자동 매핑 및 일수 계산

- **v0.3** (2025-12-31) - MVP 완성
  - Service 계층 완성 (Auth, Member, Document, Approval)
  - Controller 완성 (Auth, Member, Approval)
  - Session 기반 인증 시스템 완료
  - BCrypt 비밀번호 암호화
  - API 가이드 문서 작성

- **v0.2** (2025-12-31)
  - Mapper 전체 완성 (7개)
  - MyBatis XML 매핑 완료

- **v0.1** (2025-12-31)
  - 데이터베이스 스키마 생성 (12개 테이블)
  - Domain 클래스 생성 (9개)
  - 핵심 Mapper 생성
