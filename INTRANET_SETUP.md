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

- **v0.28** (2026-01-20) - 경비 신청/미확인 경비 관리 시스템 구축 🆕
  - **경비 신청 워크플로우**:
    - expense-report_intranet.html에서 "경비 신청" 버튼 추가
    - 사용자가 월별 경비 입력 후 신청 시:
      1. EXPENSE_ITEMS_INTRANET (DETAIL) → EXPENSE_ITEMS (MASTER)로 데이터 복사
      2. EXPENSE_ITEM_READ_STATUS 테이블에 경영관리 Unit 팀원별 읽음 상태 생성
      3. EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID에 READ_STATUS.ID 연결
    - 신청 년/월(yyyy, mm) 파라미터로 EXPENSE_ITEMS에 저장

  - **미확인 경비 관리 (admin.html)**:
    - 경비보고서 관리 탭에 "미확인 보고서" 섹션 추가
    - 경영관리 Unit 팀원에게만 미확인 경비 노출
    - 미확인 경비 클릭 시 상세 모달 표시 및 읽음 처리
    - 상단 탭에 미확인 경비 개수 배지 표시

  - **새 테이블 생성 (DB)**:
    ```sql
    -- EXPENSE_ITEM_READ_STATUS 테이블
    CREATE TABLE EXPENSE_ITEM_READ_STATUS (
        ID                 NUMBER PRIMARY KEY,
        EXPENSE_ITEM_ID    NUMBER,  -- EXPENSE_ITEMS_INTRANET.ID 참조
        READER_MEMBER_ID   NUMBER,  -- 읽는 사람 (경영관리 Unit 팀원)
        READ_YN            CHAR(1) DEFAULT 'N',
        READ_AT            TIMESTAMP,
        CREATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UPDATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    -- EXPENSE_ITEMS_INTRANET에 컬럼 추가
    ALTER TABLE EXPENSE_ITEMS_INTRANET ADD EXPENSE_READ_ID NUMBER;

    -- EXPENSE_ITEMS에 년/월 컬럼 추가
    ALTER TABLE EXPENSE_ITEMS ADD YYYY VARCHAR2(4);
    ALTER TABLE EXPENSE_ITEMS ADD MM VARCHAR2(2);
    ```

  - **새 도메인 클래스**:
    - ExpenseItemReadStatus.java: 읽음 상태 도메인 (id, expenseItemId, readerMemberId, readYn, readAt)
    - ExpenseStatsDto.java: 경비 통계 DTO (totalCount, totalAmount, categoryStats)
    - UnreadExpenseDto.java: 미확인 경비 DTO (expenseItemId, submitterName, items, itemCount)

  - **ExpenseItemIntranet.java 수정**:
    - expenseReadId 필드 추가 (EXPENSE_ITEM_READ_STATUS.ID와 연동)

  - **ExpenseItem.java (schedule) 수정**:
    - usageDateStr 필드 추가 (VARCHAR2(20) 컬럼용 문자열 날짜)
    - yyyy, mm 필드 추가 (신청 년/월)

  - **새 Mapper 인터페이스**:
    - ExpenseItemReadStatusMapper.java:
      - insert(), insertWithId(), insertBatch()
      - findByReaderMemberId(), findUnreadByReaderMemberId()
      - markAsRead(), markAsReadById()
      - countUnreadByReaderMemberId(), getNextId()

  - **ExpenseItemIntranetMapper.java 수정**:
    - findByExpenseReadId(): EXPENSE_READ_ID로 경비 항목 조회
    - updateExpenseReadId(): 단일 항목 EXPENSE_READ_ID 업데이트
    - updateExpenseReadIdBatch(): 여러 항목 일괄 업데이트

  - **ExpenseItemMapper.java (schedule) 수정**:
    - insertWithId(): ID를 직접 지정하여 INSERT (EXPENSE_ITEMS_INTRANET.ID 사용)
    - findByDateRange(), findByMemberIdAndDateRange(): 날짜 범위 조회

  - **ExpenseItemIntranetService.java 수정**:
    - submitExpenseItems(): 경비 신청 처리 (3개 테이블 연동)
    - getUnreadExpenses(): 미확인 경비 조회
    - markExpenseAsRead(): 읽음 처리
    - getUnreadCount(): 미확인 경비 개수 조회
    - getExpenseStats(): 경비 통계 조회 (년/월별, 카테고리별)

  - **ExpenseReportIntranetController.java 수정**:
    - POST /items/submit: 경비 신청 API
    - GET /items/unread: 미확인 경비 조회 API
    - POST /items/{itemId}/mark-read: 읽음 처리 API
    - GET /items/unread-count: 미확인 경비 개수 API
    - GET /items/stats: 경비 통계 API
    - GET /items/by-read-id/{readStatusId}: READ_STATUS.ID로 경비 항목 조회

  - **Mapper XML 파일**:
    - ExpenseItemIntranetMapper.xml: findByExpenseReadId, updateExpenseReadId, updateExpenseReadIdBatch 쿼리 추가
    - ExpenseItemReadStatusMapper.xml: 전체 CRUD 쿼리 (신규)
    - ExpenseItemMapper.xml: insertWithId 쿼리 추가

  - **expense-report_intranet.html 수정**:
    - "경비 신청" 버튼 추가 (btnSubmitExpense)
    - submitExpense() 함수: 선택된 월의 경비 항목을 신청
    - updateExpenseSubmitButton(): 신청 여부에 따른 버튼 표시/숨김 제어
    - checkExpenseSubmitted(): EXPENSE_ITEMS에서 신청 여부 확인

  - **admin.html 수정**:
    - 경비보고서 관리 탭에 미확인 경비 섹션 추가
    - loadUnreadExpenses(): 미확인 경비 목록 로드
    - renderUnreadExpenses(): 미확인 경비 렌더링
    - loadExpenseBadge(): 탭 배지 업데이트

  - **데이터 흐름**:
    ```
    [expense-report_intranet.html]
         │
         ▼ 경비 신청 클릭
    [ExpenseItemIntranetService.submitExpenseItems()]
         │
         ├─► EXPENSE_ITEMS_INTRANET 조회
         │
         ├─► EXPENSE_ITEMS INSERT (ID 동일하게 사용)
         │
         ├─► EXPENSE_ITEM_READ_STATUS INSERT (경영관리 Unit 팀원 수만큼)
         │   └─ 동일한 ID로 여러 ROW 생성
         │
         └─► EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID 업데이트

    [admin.html - 경영관리 Unit 팀원]
         │
         ▼ 경비보고서 관리 탭 진입
    [loadUnreadExpenses()]
         │
         ├─► EXPENSE_ITEM_READ_STATUS에서 READ_YN='N' 조회
         │
         └─► EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID로 상세 조회
    ```

- **v0.27** (2026-01-20) - 문서 작성 페이지 일정 유형 확장
  - **결재 대기함 상세 수정**:
    - 문서 유형에따라 한글 표기되도록 수정
  - **새 문서 유형 추가**:
    - 휴일근무신청서 (HOLIDAY_WORK)
    - 공가신청서 (OFFICIAL_LEAVE)
    - 방범신청서 (SECURITY_REQUEST)
    - 기존: 휴가신청서 (VACATION/HALF_DAY), 경비보고서 (EXPENSE), 일반문서 (GENERAL)

  - **문서-일정 연동 흐름**:
    1. document-create.html에서 새 문서 유형 선택 및 작성
    2. 결재 상신 시 schedules_intranet 테이블에 PENDING 상태로 일정 생성
    3. 결재 승인 시 일정 상태 APPROVED로 변경 → 캘린더에 표시
    4. 결재 반려 시 일정 상태 REJECTED로 변경

  - **수정 파일**:
    - DocumentIntranet.java: DocumentType enum에 HOLIDAY_WORK, OFFICIAL_LEAVE, SECURITY_REQUEST 추가
    - DocumentIntranetController.java: 새 문서 유형별 조건 처리 추가
    - document-create.html: 새 문서 유형 select option 및 입력 필드 추가 (이전 세션)
    - ApprovalService.java: createScheduleFromVacationDocument()에서 새 유형 지원 (이전 세션)
    - ScheduleIntranetMapper.xml: findByDateRange 등 쿼리에 HOLIDAY_WORK 날짜 필드 지원 (이전 세션)
    - approval-pending.html: 배지 및 상세보기 한글 표시 지원 (이전 세션)

  - **DB 변경 필요**:
    ```sql
    -- 기존 제약조건 삭제
    ALTER TABLE documents_intranet DROP CONSTRAINT CHK_DOC_INTRA_TYPE;

    -- 새 제약조건 추가 (새 문서 유형 포함)
    ALTER TABLE documents_intranet ADD CONSTRAINT CHK_DOC_INTRA_TYPE
    CHECK (document_type IN ('LEAVE', 'EXPENSE', 'GENERAL', 'HOLIDAY_WORK', 'OFFICIAL_LEAVE', 'SECURITY_REQUEST'));
    ```

- **v0.26** (2026-01-19) - 일정 중복 등록 방지 기능 추가
  - **방범신청 시간대 중복 방지**:
    - 같은 날짜에 시간대가 겹치는 승인된 방범신청이 있으면 등록 불가
    - 시간 겹침 판정: `start_time < endTime AND end_time > startTime`
    - 에러 메시지: "해당 시간대에 이미 승인된 방범신청이 있습니다. (2026-01-20 09:00~12:00, 신청자: 홍길동)"

  - **사용자별 일정 중복 방지**:
    - 같은 사용자가 같은 일정 유형으로 날짜 범위가 겹치는 일정 등록 불가
    - 취소(CANCELLED), 반려(REJECTED) 상태는 제외
    - 에러 메시지: "해당 기간에 이미 등록된 연차이(가) 있습니다. (2026-01-20 ~ 2026-01-22)"

  - **휴일근무 중복 방지**:
    - 같은 사용자가 휴일근무일 또는 대체휴무일이 겹치는 휴일근무 등록 불가
    - 교차 검증: 기존 휴일근무일 = 신규 대체휴무일, 기존 대체휴무일 = 신규 휴일근무일
    - 에러 메시지: "이미 등록된 휴일근무와 날짜가 겹칩니다. (휴일근무일: 2026-01-25, 대체휴무일: 2026-01-27)"

  - **파일 수정 내역**:
    - ScheduleIntranetMapper.java: 3개 중복 체크 메서드 추가
      - `findApprovedSecurityRequests()` - 방범신청 시간대 중복 체크
      - `findDuplicateSchedules()` - 일반 일정 중복 체크
      - `findDuplicateHolidayWork()` - 휴일근무 중복 체크
    - ScheduleIntranetMapper.xml: 3개 SQL 쿼리 추가
    - ScheduleIntranetService.java: `validateScheduleDuplication()` 검증 메서드 추가

- **v0.25** (2026-01-15) - 날짜 형식 호환성 개선
  - **HTML5 date input 호환성 문제 해결**:
    - application.yml의 Jackson 설정으로 API가 `yyyy-MM-dd HH:mm:ss` 형식 반환
    - HTML5 date input은 `yyyy-MM-dd` 형식만 허용
    - 문제: 일정 상세 페이지에서 시작일/종료일이 표시되지 않음
    - 콘솔 에러: "The specified value '2026-01-19 00:00:00' does not conform to the required format, 'yyyy-MM-dd'"

  - **해결 방법**:
    - schedule-calendar.html의 날짜 포맷 함수 수정
    - `split('T')[0]` → `split(/[T ]/)[0]` (정규식 사용)
    - 'T' 또는 공백 문자 모두 처리하여 날짜 부분만 추출
    - ISO 8601 형식(`2026-01-19T00:00:00`)과 새 형식(`2026-01-19 00:00:00`) 모두 지원

  - **수정된 위치**:
    - schedule-calendar.html Line 1510: showEventDetail() 함수 내 formatDate()
    - schedule-calendar.html Line 2497: loadMySchedules() 함수 내 휴가 배지 formatDateOnly()

  - **효과**:
    - 일정 상세 페이지: 시작일/종료일 정상 표시
    - 휴가 배지: `2026-01-19 00:00:00` → `2026-01-19` (시간 제거)

- **v0.24** (2026-01-15) - Jackson 전역 타임존 설정
  - **타임존 UTC → Asia/Seoul 전환**:
    - Spring Boot의 Jackson 라이브러리가 기본적으로 java.util.Date를 UTC로 직렬화하는 문제 해결
    - 문제 현상: DB에 `2026-01-19 00:00:00` 저장 → API 응답 `"2026-01-18T15:00:00.000+00:00"` (9시간 차이)
    - application.yml에 전역 타임존 설정 추가
    - `spring.jackson.time-zone=Asia/Seoul`
    - `spring.jackson.date-format=yyyy-MM-dd HH:mm:ss` (24시간 형식)

  - **영향받는 API**:
    - `/api/intranet/schedules` - 일정 조회 (startDate, endDate)
    - `/api/intranet/approvals` - 결재 조회 (createdAt, updatedAt)
    - `/api/intranet/documents` - 문서 조회 (모든 timestamp 필드)
    - `/api/intranet/recent-activities` - 최근 활동 (timestamp)
    - 기타 모든 Date/Timestamp 필드 반환 API

  - **장점**:
    - 단일 설정으로 전체 애플리케이션 타임존 통일
    - 도메인 클래스마다 @JsonFormat 어노테이션 추가 불필요
    - 일관된 날짜/시간 처리로 유지보수성 향상

  - **파일 수정 내역**:
    - application.yml: spring.jackson 설정 추가

- **v0.23** (2026-01-15) - 휴가 배지 표시 개선
  - **휴가 배지 날짜 형식 정리**:
    - 일정/휴가관리 페이지 "내 일정" 목록의 휴가 배지 날짜 포맷 수정
    - ISO DateTime 형식(2026-01-16T15:00:00)에서 날짜만 추출(2026-01-16)
    - split('T')[0]로 시간 부분 제거
    - 변경 전: `📅 연차 | 2026-01-16T15:00:00 ~ 2026-01-16T15:00:00 | 1일`
    - 변경 후: `📅 연차 | 2026-01-16 ~ 2026-01-16 | 1일`
    - 완료 문서함 배지와 동일한 형식으로 통일

  - **휴가 배지 표시 위치 추가**:
    - approval-pending.html > 완료 문서함 탭: 휴가 배지 생성 로직 추가
    - schedule-calendar.html > 내 일정 목록: 휴가 배지 생성 로직 추가
    - 기존 결재 대기 탭과 동일한 스타일 및 로직 적용
    - 배지 스타일: 노란색 배경(#fef3c7), 갈색 텍스트(#92400e)

  - **파일 수정 내역**:
    - schedule-calendar.html (Lines 2486-2494): loadMySchedules() 함수 내 배지 날짜 형식 수정
    - approval-pending.html (Lines 1230-1235): displayCompletedApprovals() 함수 내 배지 생성 추가

- **v0.22** (2026-01-15) - 관리자 페이지 일정/휴가 관리 대규모 개선
  - **관리자 페이지 전체 구조 (admin.html)**:
    - 3개 탭 시스템: 일정/휴가 관리, 경비보고서 관리, 사원 관리
    - 탭별 알림 배지 표시 (notification-badge)
    - switchTab(tabIndex) 함수로 탭 전환
    - 각 탭마다 독립적인 통계 카드 및 필터
    - 반응형 레이아웃 (모바일 최적화)
    - 헤더: Y&C Intranet 로고, 사용자 아바타, 로그아웃 버튼

  - **일정/휴가 관리 탭 구조**:
    - 3개 카드 시스템:
      - 전사 일정 카드: 금일 일정 건수 (클릭 시 캘린더 표시)
      - 임직원 연차 현황 카드: 활성 구성원 수 (클릭 시 테이블 표시)
      - 결재 대기 카드: 승인 대기 중인 일정 건수
    - selectScheduleCard(cardType) 함수로 카드별 뷰 전환
    - 카드 선택 시 시각적 강조 (selected 클래스)
    - 동적 필터 및 테이블/캘린더 토글

  - **일정 색상 및 데이터 규칙 통일**:
    - schedule-calendar.html과 동일한 색상 체계 적용
    - 연차(VACATION): #ec4899 (핑크)
    - 반차(HALF_DAY): #8b5cf6 (보라)
    - 출장(BUSINESS_TRIP): #3b82f6 (파랑)
    - 그라데이션 제거, 순색으로 통일
    - eventDidMount 콜백으로 강제 색상 적용 (CSS 우선순위 문제 해결)
    - !important 규칙 추가로 FullCalendar 기본 스타일 오버라이드

  - **RESERVED 상태 지원**:
    - 출장 일정의 RESERVED 상태를 APPROVED와 동일하게 처리
    - 필터링 로직에 RESERVED 조건 추가
    - 전사 일정 카운트에 RESERVED 포함

  - **캘린더 UI 개선**:
    - month 버튼 제거 (headerToolbar right: '')
    - today 버튼 텍스트 "오늘"로 변경 (buttonText)
    - 달력 하단 border 잘림 현상 수정 (.fc-scrollgrid 테두리 추가)

  - **한국 공휴일 표시 기능**:
    - 2025-2026년 공휴일 데이터 추가 (신정, 설날, 삼일절, 어린이날, 부처님오신날, 현충일, 광복절, 추석, 개천절, 한글날, 성탄절)
    - getKoreanHolidays(year) 함수 구현
    - isHoliday(date) 함수로 공휴일 여부 판단
    - UTC 타임존 문제 해결: toISOString() 대신 로컬 날짜 기준 문자열 생성
    - dayCellDidMount 콜백에서 공휴일 스타일 적용
    - 공휴일 텍스트 빨간색 (#e53e3e), 굵은 글씨 (font-weight: 700)
    - 공휴일 이름 표시: "신정 1일" 형식 (날짜 좌측에 공휴일 이름)
    - insertBefore로 요소 순서 조정
    - inline style 적용으로 달력 네비게이션 시에도 색상 유지

  - **주말 날짜 색상 적용**:
    - 토요일 날짜: 파란색 (#3b82f6)
    - 일요일 날짜: 빨간색 (#ef4444)
    - .fc-day-sat, .fc-day-sun 클래스 활용
    - font-weight: 700 적용

  - **전사 일정 카드 표기 개선**:
    - 기존: "전사 일정 0건"
    - 변경: "금일 일정 0 건" (숫자만 span 태그로 분리)
    - 금일 기준 APPROVED 또는 RESERVED 상태만 집계
    - 날짜 범위 비교 로직 추가 (시작일 <= 오늘 <= 종료일)

  - **임직원 연차 현황 개선**:
    - 보유 연차 컬럼: members_intranet.annual_leave_granted 연동
    - "이력 확인" 컬럼 및 버튼 추가
    - 연차 이력 팝업 구현:
      - 사원별 연차/반차 사용 이력 조회
      - APPROVED 상태만 필터링
      - 반차: 0.5일, 연차: 날짜 차이 계산
      - 테이블: 이름, 시작일, 종료일, 사용일수
      - 하단 총 사용일수 요약
      - 이력 없을 시 "사용된 이력이 없습니다" 메시지
    - showLeaveHistory(memberId, memberName) 함수 구현
    - 팝업 중앙 정렬 (modal-content margin: 50px auto)
    - 닫기 버튼(×) 스타일 통일 (사원 등록 팝업과 동일)

  - **테이블 스타일 개선**:
    - 연차 이력 테이블에 border 추가
    - th, td에 border: 1px solid #e2e8f0
    - padding: 12px
    - 요약 영역 배경색 및 우측 정렬

  - **버튼 스타일 추가**:
    - .btn-history 클래스 추가 (초록색 그라데이션)
    - padding: 6px 16px
    - border-radius: 6px
    - 호버 효과: transform scale(1.05), 그림자 증가

  - **모달 자동 닫기 방지**:
    - 사원 등록/수정 팝업의 window.onclick 이벤트 핸들러 비활성화
    - 팝업 영역 외부 클릭 시 자동으로 닫히지 않도록 변경
    - 연차 이력 팝업과 동일한 UX 제공
    - 닫기 버튼(×) 또는 취소 버튼으로만 닫을 수 있음

  - **기술 스택**:
    - FullCalendar 6.1.10: eventDidMount, dayCellDidMount 콜백 활용
    - CSS 우선순위: !important, inline style
    - JavaScript Date API: 로컬 시간 기준 날짜 처리
    - 정규식: 날짜 범위 비교, 공휴일 매칭
    - Array.filter(): 일정 필터링, 이력 조회
    - Array.reduce(): 총 사용일수 계산
    - insertBefore(): DOM 요소 순서 조정

  - **버그 수정 이력**:
    - 반차 색상 불일치: eventDidMount에서 scheduleType 기준 색상 직접 설정
    - 공휴일 날짜 오차: UTC 변환 제거, 로컬 날짜 기준 문자열 생성
    - 공휴일 색상 사라짐: inline style 적용으로 해결
    - 공휴일 위치: appendChild → insertBefore로 변경

  - **경비보고서 관리 탭 추가**:
    - 통계 카드:
      - 승인 대기 중인 경비보고서 건수
      - 이번 달 총 경비 금액
      - 이번 달 보고서 건수
    - 기능:
      - 승인 대기 중인 경비보고서 관리
      - 전체 경비 내역 조회
      - Empty state 표시 (💰 아이콘)

  - **사원 관리 탭 개선**:
    - 통계 카드 추가:
      - 전체 사원 수
      - 활성 사원 수 (재직)
      - 비활성 사원 수 (퇴사)
      - 카드 클릭 시 필터링 (all/active/inactive)

    - 필터링 기능:
      - 본부별 필터 (2단계 계층 구조)
      - 부서별 필터 (본부 선택 시 동적 로딩)
      - 통합 검색 (이름, 이메일, 부서명)
      - onParentDeptChange() 함수로 계층적 필터링

    - 사원 등록 모달:
      - 4개 섹션 구조: 기본 정보, 조직/직무 정보, 계정/권한, 근무/연차 정보
      - 필수 입력 필드 표시 (빨간색 *)
      - 본부-부서 연동 선택 (loadDepartmentsByDivision)
      - 직급 선택: 본부장, 연구소장, 전문위원, Unit장, 매니저
      - 권한 선택: USER, APPROVER, ADMIN
      - 연차 부여일수: 기본 15일 (0~30일, 0.5일 단위)
      - 입사일 선택 (max: 9999-12-31)

    - 사원 수정 모달:
      - 이름, 이메일 수정 불가 (disabled, 회색 배경)
      - 입사일 수정 불가
      - 상태 변경 가능 (활성/비활성)
      - 전화번호, 본부, 부서, 직급, 권한, 연차 수정 가능
      - loadDepartmentsByDivisionForEdit() 함수로 수정 시 부서 로딩

    - 폼 검증:
      - HTML5 required 속성 활용
      - placeholder로 입력 가이드 제공
      - small 태그로 추가 설명 제공
      - 비밀번호는 등록 시에만 입력 (수정 시 불필요)

    - 테이블 표시:
      - 사원 목록 테이블
      - 로딩 스피너 표시
      - memberTableContent에 동적 렌더링
      - 수정/삭제 버튼 제공

  - **파일 수정 내역**:
    - admin.html (전체 파일 신규 작성)
      - Lines 352-777: 사원 관리 스타일
      - Lines 1130-1179: 사원 관리 탭 구조
      - Lines 1183-1278: 사원 등록 모달
      - Lines 1280-1379: 사원 수정 모달
      - Lines 659-714, 797-807, 856-903: 일정/휴가 관리 스타일
      - Lines 1031-1093: 일정/휴가 관리 카드 및 테이블
      - Lines 2139-2328: 일정/휴가 관리 JavaScript 로직
      - Lines 2929-3028: 연차 이력 조회 기능

- **v0.21** (2026-01-14) - UI 개선, 알림 시스템 확장, 첨부파일 개선
  - **첨부파일 UUID 제거**:
    - 파일명에서 UUID 부분 제거하여 원본 파일명만 표시
    - `getOriginalFileName()` 함수 추가 (approval-pending.html)
    - 정규식 패턴: `/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_/i`
    - 변경 전: `b69fc53c8-211c-4b5c-98e2-186a4a22d06d_파일명.xlsx`
    - 변경 후: `파일명.xlsx`

  - **일정 모달 버튼 표시 버그 수정** (schedule-calendar.html):
    - 이전 일정 상세보기 후 새 일정 추가 시 "저장 및 결재요청" 버튼 안보이는 문제 해결
    - `closeEventModal()` 함수에서 `submitBtn.style.display = 'inline-block'` 추가
    - 모달 닫을 때 버튼 display 속성 초기화

  - **반려 사유 선택사항으로 변경**:
    - 승인/반려 모두 사유 입력 없이 처리 가능하도록 변경
    - ApprovalService.reject() 메서드에서 사유 필수 검증 제거
    - 사유 없을 경우 빈 문자열로 저장

  - **내 일정 목록 성능 최적화** (schedule-calendar.html):
    - 전체 일정 조회 → 이번 달 + 다음 달 일정만 조회
    - `loadMySchedules()` 함수 수정: startDate/endDate 파라미터 추가
    - 성능 개선: 불필요한 과거 데이터 조회 방지

  - **전체 페이지 알림 시스템 확장**:
    - 공통 모듈화: `/css/notification-bell.css`, `/js/notification-bell.js` 생성
    - 알림 벨 추가된 페이지:
      - approval-pending.html (내 결재 대기함)
      - schedule-calendar.html (일정/휴가관리)
      - document-create.html (문서작성)
      - my-documents.html (내문서함)
      - expense-report_intranet.html (경비보고서)
    - 경비보고서 페이지 헤더 신규 추가:
      - Y&C Intranet 로고
      - 알림 벨 (🔔)
      - 사용자 아바타
      - 로그아웃 버튼
    - 알림 기능:
      - 읽지 않은 알림 개수 배지 표시
      - 알림 드롭다운 (클릭 시 목록 표시)
      - 모두 읽음 처리
      - 알림 삭제
      - 30초마다 자동 갱신
      - 알림 클릭 시 해당 페이지로 이동

  - **헤더 UI 통일화**:
    - 메인 페이지 헤더 간소화: 사용자 이름/직급 제거, 아바타만 표시
    - 모든 페이지 헤더 구조 통일:
      - 좌측: Y&C Intranet 로고 (클릭 시 메인으로 이동)
      - 우측: 알림 벨 → 사용자 아바타 → 로그아웃 버튼
    - 사용자 아바타: 이름 첫 글자만 표시

  - **기술 스택**:
    - JavaScript 모듈화: 알림 기능 공통 JS 파일로 분리
    - CSS 재사용: 알림 스타일 공통 CSS 파일로 분리
    - 정규식 활용: UUID 패턴 제거
    - sessionStorage 활용: 사용자 정보 관리

- **v0.20** (2026-01-13) - 문서작성, 결재 대기함, 일정/휴가 관리 개선
  - **문서 작성 페이지 개선**:
    - 연차/반차 신청 시 날짜 선택 기능 추가
    - 일수 자동 계산 및 수동 지정 기능
    - 휴가 유형별 상세 입력 폼 개선
    - 날짜 범위 선택 UI 추가
    - 반차 시 오전/오후 선택 옵션
    - **날짜 기본값 자동 설정**: 연차/반차 선택 시 시작일/종료일 자동으로 내일 날짜로 설정
    - **반차 일수 자동 입력**: 반차 선택 시 0.5일로 자동 설정

  - **내 결재 대기함 UI 개선**:
    - 첨부파일 목록 표시 기능 추가
    - 첨부파일 다운로드 기능 구현
    - 파일명, 파일 크기 표시
    - 여러 파일 동시 첨부 지원
    - 첨부파일 미리보기 (이미지/PDF)
    - **휴가 정보 배지 표시**: 결재 대기함 목록에서 휴가 신청서의 유형, 날짜, 사용일수를 배지로 표시
    - **JSON 텍스트 제거**: `[일정정보:...]` 형태의 JSON 텍스트를 내용 미리보기에서 제거하여 깔끔한 UI 제공

  - **일정/휴가 관리 시스템 연동**:
    - 문서 작성에서 휴가신청서 제출 시 일정 자동 생성
    - ApprovalService.createScheduleFromVacationDocument() 메서드 추가
    - 문서 content에서 일정 정보 JSON 파싱
    - 일정 정보 형식: `[일정정보:{"scheduleType":"VACATION", "startDate":"2026-01-20", "endDate":"2026-01-22", "daysUsed":3}]`
    - 정규식 기반 JSON 추출 및 파싱
    - 휴가신청서(LEAVE/VACATION/VACATION_REQUEST) 타입 자동 감지
    - 일정 생성 시 PENDING 상태로 자동 설정
    - 결재 승인/반려 시 일정 상태 자동 동기화

  - **일정 조회 필터 개선**:
    - 부서별 일정 조회 기능 추가
    - 본부별 일정 조회 기능 추가 (divisionId 기반)
    - ScheduleIntranetService.getSchedulesByDepartmentAndDateRange() 메서드
    - ScheduleIntranetService.getSchedulesByDivisionAndDateRange() 메서드
    - ScheduleIntranetMapper에 findByDepartmentAndDateRange, findByDivisionAndDateRange 쿼리 추가
    - 날짜 범위 필터링 지원

  - **일정 상태 동기화 로직 개선**:
    - ApprovalService.syncScheduleStatus() 메서드 최적화
    - findAll() 대신 findByDocumentId() 사용으로 성능 개선
    - 디버깅 로그 추가 (일정 동기화 시작/완료/실패)
    - 연차/반차만 선택적으로 상태 동기화
    - 일정 업데이트 건수 로깅

  - **첨부파일 시스템 버그 수정**:
    - **Excel 파일 업로드 오류 해결**: `ORA-12899` 에러 (FILE_TYPE 컬럼 길이 초과)
    - DB 스키마 수정: `attachments_intranet.file_type` VARCHAR2(50) → VARCHAR2(100)
    - Excel MIME 타입 길이: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` (73자)
    - Content-Type null 처리: 확장자 기반 자동 설정 로직 추가
    - AttachmentService.uploadFile() 개선: xlsx/xls 확장자별 Content-Type 자동 매핑

  - **기술 스택**:
    - 정규식 패턴 매칭: `\\[일정정보:(.+?)\\]`
    - JSON 파싱: 간단한 정규식 기반 파싱 (Jackson 미사용)
    - SimpleDateFormat: 날짜 문자열 파싱
    - MyBatis 쿼리 최적화: findByDocumentId 인덱스 활용
    - 로그 추가: 디버깅 및 모니터링 개선
    - JavaScript Date API: 내일 날짜 자동 계산 및 설정

- **v0.19** (2026-01-13) - 실시간 알림 시스템 구현
  - **알림 벨 기능 추가**:
    - intranet-main.html 헤더에 알림 벨 아이콘 추가
    - 읽지 않은 알림 개수 실시간 표시 (빨간색 배지)
    - 30초마다 자동 갱신 (setInterval)
    - 클릭 시 드롭다운 메뉴로 알림 목록 표시

  - **알림 데이터베이스 구조**:
    - notifications_intranet 테이블 생성
    - 필드: id, member_id, notification_type, title, content, link_url, is_read, created_at, read_at
    - 알림 타입: APPROVAL_REQUEST, APPROVAL_APPROVED, APPROVAL_REJECTED, LEAVE_APPROVED, LEAVE_REJECTED
    - CASCADE DELETE: 회원 삭제 시 알림도 자동 삭제

  - **알림 생성 로직 (NotificationService.java)**:
    - createApprovalRequestNotification() - 결재 요청 시 (문서 상신, 휴가 신청)
    - createApprovalApprovedNotification() - 결재 승인 시
    - createApprovalRejectedNotification() - 결재 반려 시 (반려 사유 포함)
    - createLeaveApprovedNotification() - 휴가 승인 시
    - createLeaveRejectedNotification() - 휴가 반려 시
    - Try-catch 처리로 알림 실패 시에도 핵심 로직 영향 없음

  - **알림 연동 포인트**:
    - DocumentIntranetService.submitDocument() - 문서 상신 시 결재자에게 알림
    - ScheduleIntranetService.createSchedule() - 휴가 신청 시 결재자에게 알림
    - ScheduleIntranetService.requestCancellation() - 취소 신청 시 결재자에게 알림
    - ApprovalService.approve() - 승인 시 기안자에게 알림
    - ApprovalService.reject() - 반려 시 기안자에게 알림 (반려 사유 포함)

  - **알림 REST API (NotificationController.java)**:
    - GET /api/intranet/notifications - 내 알림 목록 조회
    - GET /api/intranet/notifications/unread-count - 읽지 않은 알림 개수
    - POST /api/intranet/notifications/{id}/read - 알림 읽음 처리
    - POST /api/intranet/notifications/read-all - 전체 읽음 처리
    - DELETE /api/intranet/notifications/{id} - 알림 삭제
    - POST /api/intranet/notifications - 알림 생성 (내부 API)

  - **프론트엔드 기능**:
    - 알림 클릭 시 해당 페이지로 자동 이동
    - 링크 URL: /approval-pending.html, /my-documents.html, /schedule.html (절대 경로)
    - 읽음 처리 후 배지 카운트 자동 감소
    - "모두 읽음" 버튼으로 전체 읽음 처리
    - 알림 삭제 버튼 (✕) 추가 - 개별 알림 삭제 가능
    - 시간 표시: "방금 전", "5분 전", "2시간 전", "3일 전" 형식
    - 읽지 않은 알림 시각적 강조 (배경색 변경)

  - **알림 드롭다운 UI**:
    - 최대 높이 400px, 스크롤 가능
    - 알림 항목별 클릭 영역과 삭제 버튼 분리
    - 삭제 버튼 호버 시 빨간색 강조
    - 빈 알림 시 "알림이 없습니다" 메시지 표시
    - 외부 클릭 시 자동 닫힘

  - **버그 수정 이력**:
    - Schedule ID null 문제: scheduleMapper.insert() 후 알림 생성하도록 순서 변경
    - 취소 알림 링크 오류: scheduleId 대신 cancelDocument.getId() 사용
    - 404 에러: 상대 경로를 절대 경로로 변경 (/approval-pending.html)

  - **기술 스택**:
    - MyBatis Mapper XML: NotificationIntranetMapper.xml
    - Spring Boot @Service: NotificationService
    - Spring Boot @RestController: NotificationController
    - 세션 기반 인증: HttpSession의 "userId" 속성 사용
    - JavaScript: Fetch API, setInterval (30초 폴링)
    - CSS: 알림 배지, 드롭다운, 호버 효과

- **v0.18** (2026-01-12) - 메인 페이지 최근 활동 탭 기능 구현
  - **최근 활동 API 컨트롤러 추가**:
    - ActivityController.java 신규 생성
    - `/api/intranet/activity/recent` 엔드포인트 구현
    - 세션 기반 사용자 인증 (userId 속성)
    - 최근 활동 3가지 조회:
      - 📝 최근 작성한 문서 5건
      - ✅ 최근 처리한 결재 5건 (승인/반려만)
      - 📅 최근 일정/휴가 5건

  - **메인 페이지 탭 UI 구현**:
    - intranet-main.html에 탭 구조 추가
    - 3개 탭 전환 기능 (문서/결재/일정)
    - CSS 기반 탭 활성화 표시
    - 각 항목 클릭 시 해당 페이지로 이동

  - **문서 타입 한글 표시 (LEAVE 타입 파싱)**:
    - 문서 제목에서 휴가 타입 자동 추출
    - 지원 타입: 연차, 오전반차, 오후반차, 반차, 휴가(기본값)
    - `getDocumentTypeLabel(type, title)` 함수 구현
    - `includes()` 메서드로 키워드 검색

  - **일정 시간 정보 표시**:
    - 시간 있는 경우 (회의/행사):
      - 같은 날: `1월 12일 14:00 ~ 16:00`
      - 다른 날: `1월 12일 14:00 ~ 1월 13일 16:00`
    - 시간 없는 경우 (휴가):
      - 하루: `1월 12일`
      - 여러 날: `1월 12일 ~ 1월 15일`
    - `formatScheduleDateTime(schedule)` 함수 구현

  - **버그 수정 이력**:
    - Mapper 임포트 오류: ScheduleMapper → ScheduleIntranetMapper
    - 세션 인증 오류: loginUser → userId 속성명 변경
    - 스케줄 미표시: Spring Boot 재시작 후 정상 작동
    - 문서 타입 라벨: title 파라미터 누락 수정

  - **기술 스택**:
    - Java Streams API 활용 (.stream().limit(5))
    - 세션 기반 인증 (ApprovalController와 일관)
    - CSS 탭 전환 (display: none/block)
    - 문자열 파싱 (includes 메서드)

- **v0.17** (2026-01-09) - 네이버웍스 로그인 자동 사용자 생성 및 메시지 개선
  - **로그인 실패 시 네이버웍스 안내 메시지 추가**:
    - 기존 문제: 일반 로그인 실패 시 사용자가 다음 액션을 알 수 없음
    - 개선: 로그인 실패 메시지에 "네이버웍스 로그인을 통해 사용자 정보를 생성할 수 있습니다" 안내 추가
    - AuthController.java line 69

  - **네이버웍스 로그인 성공 시 자동 사용자 생성 로직 개선**:
    - 기존: 사용자가 DB에 없으면 자동 생성 (이미 구현됨)
    - 개선: 중복 생성 시도 등 예외 상황 방어 로직 추가
    - 동시 요청으로 인한 중복 생성 시도 시 재조회하여 멱등성 보장
    - NaverWorksAuthController.java lines 86-101

  - **네이버웍스 자동 사용자 생성 상세**:
    - 네이버웍스에서 제공하는 사용자 정보(email, name, phone, position)로 자동 생성
    - 기본 비밀번호: 1234
    - 기본 권한: USER
    - 기본 연차: 15일
    - 활성 상태: true
    - createMemberFromNaverWorks() 메서드 (NaverWorksAuthController.java lines 133-168)

  - **로그인 화면 에러 메시지 개선**:
    - user_not_found: "네이버웍스 로그인을 통해 자동으로 사용자 정보가 생성됩니다" 안내
    - user_creation_failed: 사용자 생성 실패 시 관리자 문의 안내
    - intranet-login.html lines 385-389

  - **예외 처리 및 안정성 강화**:
    - 사용자 생성 실패 시 기존 로그인 흐름에 영향 없도록 try-catch 처리
    - 중복 이메일 시도 시 MemberIntranetService에서 예외 발생 (기존 로직)
    - 참조 무결성: 부서 ID는 null로 설정 (나중에 관리자가 설정)
    - 트랜잭션 관리: MemberIntranetService.createMember()에서 @Transactional 처리

- **v0.16.1** (2026-01-09) - ScheduleIntranetService 메서드명 오류 수정
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

  - **새로운 일정 유형 추가 (v0.9.3)**:
    - 휴일근무(HOLIDAY_WORK), 공가(OFFICIAL_LEAVE), 방범신청(SECURITY_REQUEST) 유형 추가
    - 휴일근무: 전용 날짜 필드(holiday_work_date, substitute_holiday_date) 사용
    - schedules_intranet 테이블 ALTER: start_date/end_date NULL 허용으로 변경
    - 휴일근무는 start_date/end_date 대신 전용 날짜 필드 사용
    - 공가: daysUsed = 0 (휴가 차감 없음)
    - 방범신청: 회의 기반, 결재 필요
    - ScheduleIntranet.java에 @JsonFormat 추가 (yyyy-MM-dd 파싱 지원)
    - ScheduleIntranetMapper.xml에 jdbcType=DATE 추가 (NULL 허용)
    - ScheduleIntranetService.java에 날짜 필드 검증 로직 추가
      - 휴일근무: holiday_work_date, substitute_holiday_date 필수
      - 그 외: start_date, end_date 필수
    - schedule-calendar.html에 UI 및 색상 추가
      - HOLIDAY_WORK: 주황색(#f59e0b)
      - OFFICIAL_LEAVE: 청록색(#06b6d4)
      - SECURITY_REQUEST: 남색(#6366f1)

  - **신규 일정 유형 워크플로우 통합 (v0.9.4)**:
    - **캘린더 일정 유형 한글 표시**:
      - scheduleTypeLabels 객체에 신규 유형 추가
      - 캘린더 이벤트 제목에 영문 코드 대신 한글명 표시
      - schedule-calendar.html에 getScheduleTypeLabel() 함수 추가
      - HOLIDAY_WORK → "휴일근무", OFFICIAL_LEAVE → "공가", SECURITY_REQUEST → "방범신청"

    - **내 일정 사이드바 상태 뱃지 확장**:
      - 휴일근무/공가/방범신청도 결재 상태 뱃지 표시
      - isApprovalRequired 배열 확장: ['VACATION', 'HALF_DAY', 'HOLIDAY_WORK', 'OFFICIAL_LEAVE', 'SECURITY_REQUEST']
      - SUBMITTED 상태 → "결재대기" 뱃지
      - APPROVED 상태 → "승인" 뱃지
      - 일정 배지에 날짜 정보 표시
        - 휴일근무: "근무일: YYYY-MM-DD | 대체: YYYY-MM-DD"
        - 기타: "유형명 | 시작일 ~ 종료일 | N일" (daysUsed가 0이면 일수 생략)

    - **결재 승인/반려 후 상태 동기화 개선**:
      - ApprovalService.syncScheduleStatus() 메서드 확장
      - 결재 필요 일정 유형 조건 확장: VACATION, HALF_DAY, HOLIDAY_WORK, OFFICIAL_LEAVE, SECURITY_REQUEST
      - 결재 승인 시 schedules_intranet.status를 APPROVED로 자동 업데이트
      - 결재 반려 시 schedules_intranet.status를 REJECTED로 자동 업데이트
      - schedule-calendar.html의 confirmApproval() 함수에 loadMySchedules() 호출 추가
      - 결재 처리 후 캘린더와 내 일정 자동 새로고침

    - **휴일근무/공가/방범신청 취소 신청 기능 확장**:
      - ScheduleIntranetController 수정 (lines 246-250)
      - 취소 신청 가능 일정 유형 확장: VACATION, HALF_DAY, HOLIDAY_WORK, OFFICIAL_LEAVE, SECURITY_REQUEST
      - 에러 메시지 개선: "연차/반차만..." → "결재가 필요한 일정만 취소 신청이 가능합니다."

    - **방범신청 캘린더 색상 가시성 개선**:
      - SECURITY_REQUEST 색상 변경: #6366f1 (인디고) → #f97316 (주황색)
      - 하얀 배경에서 명도 대비 향상으로 가독성 개선
      - 내 일정 사이드바와 캘린더 뷰 모두 동일한 색상 적용

    - **휴일근무 상세보기 날짜 표시 개선**:
      - showEventDetail() 함수 수정 (schedule-calendar.html lines 1571-1578)
      - 휴일근무 유형 감지 시 holidayWorkDate, substituteHolidayDate 필드 사용
      - 일반 유형은 기존대로 startDate, endDate 필드 사용
      - 상세보기 모달에서 휴일근무 날짜 정보 정상 표시

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
