# 네이버웍스 로그인 연동 가이드

## 📋 개요
YNC INTRANET에 네이버웍스(Naver Works) OAuth 2.0 로그인을 연동하여 자동 회원 가입 기능을 제공합니다.

---

## 🎯 주요 기능

### 1. 네이버웍스 OAuth 2.0 로그인
- 네이버웍스 계정으로 직접 로그인
- SSO(Single Sign-On) 지원
- CSRF 방지 (State 파라미터)

### 2. 자동 회원 등록
- DB에 사용자가 없으면 네이버웍스 정보로 자동 등록
- 멤버 추가 화면과 동일한 정보 구조
- 기본값 자동 설정

### 3. 이중 로그인 시스템
- 일반 로그인: 이메일 + 비밀번호
- 네이버웍스 로그인: OAuth 인증

---

## 🔧 1. 네이버웍스 개발자 센터 설정

### 1.1 애플리케이션 등록
```
1. https://developers.worksmobile.com 접속
2. 콘솔 로그인
3. 앱 생성
   - 앱 이름: YNC INTRANET
   - 설명: 사내 인트라넷 시스템
```

### 1.2 Redirect URI 설정
```
개발 환경:
- http://localhost:8083/api/intranet/auth/naver-works/callback

ngrok 환경 (HTTPS 필요):
- https://YOUR-NGROK-URL.ngrok-free.dev/api/intranet/auth/naver-works/callback
```

**중요**: 네이버웍스는 HTTPS만 지원하므로 로컬 테스트 시 ngrok 사용 필수

### 1.3 OAuth Scope 설정
```
필요한 권한:
- user (사용자 기본 정보)
```

### 1.4 Client ID/Secret 발급
```
발급받은 정보를 application.yml에 저장
```

---

## ⚙️ 2. 프로젝트 설정

### 2.1 application.yml 설정

```yaml
# 네이버웍스 OAuth 설정
naverworks:
  oauth:
    client-id: YOUR_CLIENT_ID
    client-secret: YOUR_CLIENT_SECRET
    redirect-uri: https://YOUR-NGROK-URL.ngrok-free.dev/api/intranet/auth/naver-works/callback
    authorization-uri: https://auth.worksmobile.com/oauth2/v2.0/authorize
    token-uri: https://auth.worksmobile.com/oauth2/v2.0/token
    user-info-uri: https://www.worksapis.com/v1.0/users/me
```

**주의**:
- `redirect-uri`는 네이버웍스 개발자 센터에 등록한 URI와 정확히 일치해야 함
- ngrok URL 사용 시 ngrok 재시작할 때마다 URL이 변경되므로 application.yml과 개발자 센터 모두 업데이트 필요

### 2.2 ngrok 설정 (HTTPS 로컬 테스트용)

```bash
# 1. ngrok 설치 (https://ngrok.com/download)

# 2. ngrok 인증
ngrok config add-authtoken YOUR_AUTHTOKEN

# 3. ngrok 실행
ngrok http 8083

# 4. 출력된 HTTPS URL을 application.yml과 네이버웍스 개발자 센터에 등록
# 예: https://partridgelike-emilie-calorescent.ngrok-free.dev
```

**ngrok 무료 버전 제약사항**:
- URL이 재시작 시마다 변경됨
- 2시간 세션 타임아웃
- 첫 접속 시 경고 화면 ("Visit Site" 버튼 클릭 필요)

---

## 📦 3. 구현된 파일 구조

### 3.1 NaverWorksOAuthService.java
네이버웍스 OAuth 처리 서비스

**위치**: `src/main/java/com/ync/intranet/service/NaverWorksOAuthService.java`

**주요 메서드**:
```java
// 네이버웍스 로그인 URL 생성
public String getAuthorizationUrl(String state)

// Authorization Code로 Access Token 발급
public String getAccessToken(String code)

// Access Token으로 사용자 정보 조회
public Map<String, Object> getUserInfo(String accessToken)
```

**가져오는 사용자 정보**:
- email (이메일)
- name (이름)
- userId (사용자 ID)
- telephoneNumber (전화번호)
- mobilePhone (휴대폰 번호)
- department (부서)
- position (직급)
- employeeNumber (사번)

### 3.2 NaverWorksAuthController.java
네이버웍스 OAuth 인증 컨트롤러

**위치**: `src/main/java/com/ync/intranet/controller/NaverWorksAuthController.java`

**주요 엔드포인트**:

#### GET /api/intranet/auth/naver-works/login
- 네이버웍스 로그인 시작
- State 파라미터 생성 (CSRF 방지)
- 네이버웍스 인증 페이지로 리다이렉트

#### GET /api/intranet/auth/naver-works/callback
- OAuth 콜백 처리
- State 검증
- Access Token 발급
- 사용자 정보 조회
- DB 조회 → 없으면 자동 등록
- 세션 생성
- 메인 페이지로 리다이렉트

#### GET /api/intranet/auth/naver-works/status
- 로그인 상태 확인 (디버깅용)

### 3.3 intranet-login.html
로그인 페이지

**위치**: `src/main/resources/static/intranet-login.html`

**주요 UI 요소**:
- 아이디 입력란
- 비밀번호 입력란 (새로 추가)
- "로그인" 버튼 (일반 로그인)
- "네이버웍스로 로그인" 버튼 (OAuth 로그인)

**JavaScript 함수**:
```javascript
// 네이버웍스 로그인 시작
function loginWithNaverWorks() {
    window.location.href = '/api/intranet/auth/naver-works/login';
}

// OAuth 에러 처리
function checkUrlError() {
    // URL 파라미터에서 에러 확인
    // invalid_state, no_email, user_not_found, user_inactive,
    // login_failed, user_creation_failed
}
```

---

## 🔄 4. OAuth 흐름

### 전체 프로세스

```
1. 사용자가 "네이버웍스로 로그인" 버튼 클릭
   ↓
2. GET /api/intranet/auth/naver-works/login
   - CSRF 방지용 state 생성
   - 세션에 state 저장
   ↓
3. 네이버웍스 인증 페이지로 리다이렉트
   - 사용자가 네이버웍스 계정으로 로그인
   - 권한 동의
   ↓
4. GET /api/intranet/auth/naver-works/callback?code=xxx&state=xxx
   - State 검증 (CSRF 방지)
   - Authorization Code로 Access Token 발급
   - Access Token으로 사용자 정보 조회
   ↓
5. DB에서 이메일로 사용자 조회
   - 있으면: 기존 사용자 로그인
   - 없으면: 자동 회원 등록 후 로그인
   ↓
6. 세션 생성 및 메인 페이지로 리다이렉트
```

---

## 👥 5. 자동 회원 등록 로직

### 5.1 등록 조건
- DB에 해당 이메일의 사용자가 없을 때

### 5.2 등록 정보

| 필드 | 값 | 출처 |
|------|-----|------|
| email | 네이버웍스 이메일 | OAuth API |
| password | `{이메일앞부분}@ync` (BCrypt 암호화) | 자동 생성 |
| name | 네이버웍스 이름 | OAuth API |
| phone | 휴대폰/전화번호 | OAuth API |
| departmentId | null | 나중에 관리자가 설정 |
| position | 네이버웍스 직급 | OAuth API |
| role | USER | 기본값 |
| hireDate | 오늘 날짜 | 자동 설정 |
| annualLeaveGranted | 15일 | 기본값 |
| isActive | true | 기본값 |

### 5.3 구현 코드
```java
private MemberIntranet createMemberFromNaverWorks(Map<String, Object> userInfo) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    String email = (String) userInfo.get("email");
    String name = (String) userInfo.get("name");
    String phone = (String) userInfo.get("mobilePhone");
    if (phone == null || phone.isEmpty()) {
        phone = (String) userInfo.get("telephoneNumber");
    }
    String position = (String) userInfo.get("position");

    // 기본 비밀번호 생성 (이메일 앞부분 + @ync)
    String defaultPassword = email.split("@")[0] + "@ync";
    String encodedPassword = passwordEncoder.encode(defaultPassword);

    MemberIntranet newMember = MemberIntranet.builder()
            .email(email)
            .password(encodedPassword)
            .name(name)
            .phone(phone)
            .departmentId(null)  // 나중에 설정
            .position(position)
            .role("USER")  // 기본 권한
            .hireDate(LocalDate.now())  // 오늘 날짜
            .annualLeaveGranted(BigDecimal.valueOf(15))  // 기본 15일
            .isActive(true)
            .build();

    return memberService.createMember(newMember);
}
```

---

## 🔐 6. 보안

### 6.1 CSRF 방지
- State 파라미터 사용
- 세션에 저장된 state와 콜백으로 받은 state 비교
- 일치하지 않으면 로그인 실패

### 6.2 비밀번호 암호화
- BCrypt 사용
- 자동 등록 시 기본 비밀번호도 BCrypt로 암호화

### 6.3 세션 관리
- HttpSession 사용
- 저장 정보: userId, userEmail, userName, userRole, departmentId

---

## 🧪 7. 테스트

### 7.1 준비사항
1. ngrok 실행 중
2. Spring Boot 애플리케이션 실행 중
3. 네이버웍스 개발자 센터에 redirect URI 등록 완료

### 7.2 테스트 시나리오

#### 시나리오 1: 기존 사용자 로그인
```
1. ngrok URL로 접속: https://YOUR-NGROK-URL.ngrok-free.dev/intranet-login.html
2. "Visit Site" 버튼 클릭 (ngrok 경고 화면)
3. "네이버웍스로 로그인" 버튼 클릭
4. 네이버웍스 로그인
5. 권한 동의
6. 자동으로 메인 페이지로 이동
```

#### 시나리오 2: 신규 사용자 자동 등록
```
1. DB에 없는 이메일로 네이버웍스 로그인
2. 자동으로 회원 등록
3. 로그인 완료 후 메인 페이지로 이동
4. 사원 관리 페이지에서 새 사용자 확인
5. 부서 정보 등 수동으로 업데이트
```

#### 시나리오 3: 일반 로그인 (비교용)
```
1. localhost:8083/intranet-login.html 접속
2. 아이디 + 비밀번호 입력
3. "로그인" 버튼 클릭
4. 메인 페이지로 이동
```

### 7.3 에러 처리 확인

| 에러 코드 | 의미 | 발생 조건 |
|----------|------|----------|
| invalid_state | 잘못된 요청 | State 파라미터 불일치 (CSRF 공격 의심) |
| no_email | 이메일 정보 없음 | 네이버웍스에서 이메일을 못 가져옴 |
| user_creation_failed | 회원 등록 실패 | DB 등록 중 에러 발생 |
| user_inactive | 비활성화 계정 | 계정이 isActive=false |
| login_failed | 로그인 처리 실패 | 기타 예외 발생 |

---

## 📊 8. 사후 관리

### 8.1 신규 등록 사용자 확인
```sql
-- 최근 등록된 사용자 조회
SELECT id, email, name, position, hire_date, department_id
FROM members_intranet
WHERE department_id IS NULL
  AND created_at > SYSDATE - 7
ORDER BY created_at DESC;
```

### 8.2 부서 정보 업데이트
- 사원 관리 페이지에서 수동으로 본부/부서 설정
- 필요시 직급, 권한 등도 수정

### 8.3 기본 비밀번호 변경 안내
- 자동 등록된 사용자에게 비밀번호 변경 안내
- 기본 비밀번호: `{이메일앞부분}@ync`
  - 예: sjlee@yncsmart.com → sjlee@ync

---

## 🚀 9. 배포 시 체크리스트

### 9.1 개발 환경
- [x] ngrok 설치 및 실행
- [x] application.yml에 ngrok URL 설정
- [x] 네이버웍스 개발자 센터에 ngrok URL 등록
- [x] OAuth 로그인 테스트
- [x] 자동 회원 등록 테스트

### 9.2 운영 환경
- [ ] 운영 도메인 HTTPS 설정
- [ ] application.yml에 운영 도메인 설정
- [ ] 네이버웍스 개발자 센터에 운영 도메인 등록
- [ ] 보안 검토 (Client Secret 노출 여부 등)
- [ ] 로그 모니터링 설정
- [ ] 에러 알림 설정

---

## 🔧 10. 문제 해결

### 10.1 "404 Not Found" 에러
**원인**: Controller가 로드되지 않음
**해결**: Spring Boot 재시작

```bash
cd c:\smartWork\workspace\yncIntranet
mvn spring-boot:run
```

### 10.2 "invalid_state" 에러
**원인**: State 파라미터 불일치
**해결**:
- 브라우저 쿠키 삭제
- 세션 초기화
- 다시 로그인 시도

### 10.3 ngrok URL 변경 시
**해야 할 작업**:
1. application.yml의 redirect-uri 업데이트
2. 네이버웍스 개발자 센터에서 redirect URI 업데이트
3. Spring Boot 재시작

### 10.4 비밀번호 필드가 안 보임
**원인**: 브라우저 캐시
**해결**:
- Ctrl + F5 강력 새로고침
- 시크릿/프라이빗 모드로 접속
- 브라우저 캐시 삭제

### 10.5 자동 등록 실패
**확인 사항**:
1. DB 연결 상태
2. members_intranet 테이블 제약조건
3. 서버 로그 확인
4. 네이버웍스에서 필수 정보(email, name)를 받아왔는지 확인

---

## 📞 11. 참고 자료

- **네이버웍스 API 문서**: https://developers.worksmobile.com/kr/document/
- **OAuth 2.0 가이드**: https://developers.worksmobile.com/kr/document/100500801
- **ngrok 공식 문서**: https://ngrok.com/docs

---

## 📝 12. 변경 이력

### v1.0 (2026-01-05)
- 네이버웍스 OAuth 2.0 로그인 구현
- 자동 회원 등록 기능 추가
- 이중 로그인 시스템 (일반 + OAuth)
- ngrok을 통한 로컬 HTTPS 테스트 환경 구축
- 비밀번호 입력 필드 추가 (일반 로그인)
