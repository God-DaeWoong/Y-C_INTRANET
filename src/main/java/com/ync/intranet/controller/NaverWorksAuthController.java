package com.ync.intranet.controller;

import com.ync.intranet.domain.DepartmentIntranet;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.mapper.DepartmentIntranetMapper;
import com.ync.intranet.mapper.MemberIntranetMapper;
import com.ync.intranet.service.MemberIntranetService;
import com.ync.intranet.service.NaverWorksOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 네이버웍스 OAuth 인증 컨트롤러
 * - Upsert 로직: 신규 사용자 자동 가입 + 기존 사용자 정보 업데이트
 */
@RestController
@RequestMapping("/api/intranet/auth/naver-works")
@CrossOrigin(origins = "*")
public class NaverWorksAuthController {

    private static final Logger log = LoggerFactory.getLogger(NaverWorksAuthController.class);

    private final NaverWorksOAuthService naverWorksOAuthService;
    private final MemberIntranetService memberService;
    private final MemberIntranetMapper memberMapper;
    private final DepartmentIntranetMapper departmentMapper;

    public NaverWorksAuthController(NaverWorksOAuthService naverWorksOAuthService,
                                    MemberIntranetService memberService,
                                    MemberIntranetMapper memberMapper,
                                    DepartmentIntranetMapper departmentMapper) {
        this.naverWorksOAuthService = naverWorksOAuthService;
        this.memberService = memberService;
        this.memberMapper = memberMapper;
        this.departmentMapper = departmentMapper;
    }

    /**
     * 네이버웍스 로그인 시작 (리다이렉트)
     * GET /api/intranet/auth/naver-works/login
     */
    @GetMapping("/login")
    public void login(HttpSession session, HttpServletResponse response) throws IOException {
        // CSRF 방지용 state 생성
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        System.out.println("🚀 네이버웍스 로그인 시작");
        System.out.println("  - Session ID: " + session.getId());
        System.out.println("  - 생성된 state: " + state);

        // 네이버웍스 인증 페이지로 리다이렉트
        String authUrl = naverWorksOAuthService.getAuthorizationUrl(state);
        System.out.println("  - 리다이렉트 URL: " + authUrl);
        response.sendRedirect(authUrl);
    }

    /**
     * 네이버웍스 OAuth 콜백 처리
     * GET /api/intranet/auth/naver-works/callback
     */
    @GetMapping("/callback")
    public void callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {
        try {
            System.out.println("📞 네이버웍스 콜백 수신");
            System.out.println("  - Session ID: " + session.getId());
            System.out.println("  - 전체 Query String: " + request.getQueryString());
            System.out.println("  - Code: " + code);
            System.out.println("  - State: " + state);
            System.out.println("  - Error: " + error);
            System.out.println("  - Error Description: " + error_description);

            // 에러가 있으면 로그 출력하고 리다이렉트
            if (error != null) {
                System.err.println("❌ 네이버웍스 OAuth 에러 발생!");
                System.err.println("  - Error Code: " + error);
                System.err.println("  - Description: " + error_description);
                response.sendRedirect("/intranet-login.html?error=" + error);
                return;
            }

            // code가 없으면 에러
            if (code == null || code.isEmpty()) {
                System.err.println("❌ Authorization Code가 없습니다!");
                response.sendRedirect("/intranet-login.html?error=no_code");
                return;
            }

            // Host 헤더로 localhost 여부 확인
            String host = request.getHeader("Host");
            boolean isLocalhost = host != null &&
                (host.startsWith("localhost") || host.startsWith("127.0.0.1"));
            System.out.println("  - Host: " + host);
            System.out.println("  - Localhost 여부: " + isLocalhost);

            // State 검증 (CSRF 방지)
            String savedState = (String) session.getAttribute("oauth_state");
            System.out.println("🔐 State 검증:");
            System.out.println("  - 저장된 state: " + savedState);
            System.out.println("  - 받은 state: " + state);

            // localhost 환경에서는 state 검증 건너뛰기
            if (savedState == null || !savedState.equals(state)) {
                if (isLocalhost) {
                    System.err.println("⚠️ State 불일치! (localhost 개발 환경이므로 무시)");
                } else {
                    System.err.println("❌ State 불일치! (프로덕션 환경 - 요청 거부)");
                    response.sendRedirect("/intranet-login.html?error=invalid_state");
                    return;
                }
            } else {
                System.out.println("✅ State 검증 통과");
            }

            // 1. Access Token 발급
            String accessToken = naverWorksOAuthService.getAccessToken(code);
            System.out.println("✅ Access Token 발급 완료");
            System.out.println("🔑 Access Token (테스트용): " + accessToken);
            System.out.println("📝 다음 curl 명령으로 직접 테스트 가능:");
            System.out.println("curl -X GET \"https://www.worksapis.com/v1.0/users/me\" \\");
            System.out.println("  -H \"Authorization: Bearer " + accessToken + "\"");

            // 2. 사용자 정보 조회
            Map<String, Object> userInfo = naverWorksOAuthService.getUserInfo(accessToken);
            System.out.println("📋 네이버웍스 사용자 정보: " + userInfo);

            String email = (String) userInfo.get("email");
            System.out.println("📧 이메일: " + email);

            if (email == null || email.isEmpty()) {
                System.err.println("❌ 이메일 정보 없음");
                response.sendRedirect("/intranet-login.html?error=no_email");
                return;
            }

            // 3. 사용자 Upsert (신규 가입 또는 정보 업데이트)
            MemberIntranet member = upsertMember(userInfo);

            if (member == null) {
                log.error("사용자 Upsert 실패");
                response.sendRedirect("/intranet-login.html?error=user_creation_failed");
                return;
            }

            if (!member.getIsActive()) {
                log.warn("비활성화된 계정 로그인 시도: {}", member.getEmail());
                response.sendRedirect("/intranet-login.html?error=user_inactive");
                return;
            }

            log.info("로그인 성공 - 사용자: {}, 이메일: {}", member.getName(), member.getEmail());

            // 4. 세션에 사용자 정보 저장
            session.setAttribute("userId", member.getId());
            session.setAttribute("userEmail", member.getEmail());
            session.setAttribute("userName", member.getName());
            session.setAttribute("userRole", member.getRole());
            session.setAttribute("departmentId", member.getDepartmentId());

            // 5. 메인 페이지로 리다이렉트
            response.sendRedirect("/intranet-main.html");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/intranet-login.html?error=login_failed");
        }
    }

    /**
     * 네이버웍스 로그인 상태 확인 (디버깅용)
     * GET /api/intranet/auth/naver-works/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");

        return ResponseEntity.ok(Map.of(
                "loggedIn", userId != null,
                "userId", userId != null ? userId : "",
                "email", userEmail != null ? userEmail : ""
        ));
    }

    /**
     * 사용자 Upsert: 신규 가입 또는 기존 사용자 정보 업데이트
     *
     * 로직:
     * 1. naverworks_user_id로 조회 (최우선)
     * 2. 없으면 email로 조회
     * 3. 둘 다 없으면 신규 등록
     * 4. 있으면 정보 업데이트 (name, position, phone, last_login_at)
     */
    private MemberIntranet upsertMember(Map<String, Object> userInfo) {
        try {
            String naverworksUserId = (String) userInfo.get("userId");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String phone = (String) userInfo.get("mobilePhone");
            if (phone == null || phone.isEmpty()) {
                phone = (String) userInfo.get("telephoneNumber");
            }
            String position = (String) userInfo.get("position");
            String departmentName = (String) userInfo.get("department");

            log.info("=== 사용자 Upsert 시작 ===");
            log.info("네이버웍스 userId: {}", naverworksUserId);
            log.info("이메일: {}", email);
            log.info("이름 (원본): {}", name);
            log.info("부서: {}, 직급: {}, 전화: {}", departmentName, position, phone);

            // 부서명으로 부서 ID 및 상위 본부 조회
            Long departmentId = null;
            String divisionName = null;
            if (departmentName != null && !departmentName.isEmpty()) {
                DepartmentIntranet department = departmentMapper.findByName(departmentName);
                if (department != null) {
                    departmentId = department.getId();
                    log.info("[{}] 부서 매칭 성공: {} (ID={})", email, departmentName, departmentId);

                    // 상위 본부 조회
                    if (department.getParentId() != null) {
                        DepartmentIntranet parentDepartment = departmentMapper.findById(department.getParentId());
                        if (parentDepartment != null) {
                            divisionName = parentDepartment.getName();
                            log.info("상위 본부: {} (ID={})", divisionName, parentDepartment.getId());
                        }
                    } else {
                        log.info("최상위 부서임 (본부 없음)");
                    }
                } else {
                    log.warn("⚠️ 부서명 '{}' 매칭 실패 - DB에 해당 부서 없음", departmentName);
                }
            }

            // userInfo에 부서 ID와 본부명 추가 (createMemberFromNaverWorks에서 사용)
            userInfo.put("departmentId", departmentId);
            userInfo.put("divisionName", divisionName);

            MemberIntranet member = null;

            // 1순위: naverworks_user_id로 조회
            if (naverworksUserId != null && !naverworksUserId.isEmpty()) {
                member = memberMapper.findByNaverworksUserId(naverworksUserId);
                if (member != null) {
                    log.info("기존 사용자 발견 (naverworks_user_id): ID={}, 이름={}",
                        member.getId(), member.getName());
                }
            }

            // 2순위: email로 조회 (naverworks_user_id가 없거나 못 찾은 경우)
            if (member == null && email != null) {
                member = memberMapper.findByEmail(email);
                if (member != null) {
                    log.info("기존 사용자 발견 (email): ID={}, 이름={}",
                        member.getId(), member.getName());
                }
            }

            // name이 null이거나 빈 문자열인 경우 기본값 설정
            if (name == null || name.isEmpty() || name.trim().isEmpty()) {
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                    log.warn("네이버웍스에서 name 정보 없음 - 이메일 앞부분 사용: {}", name);
                } else if (naverworksUserId != null && !naverworksUserId.isEmpty()) {
                    name = "User_" + naverworksUserId;
                    log.warn("네이버웍스에서 name 정보 없음 - userId 사용: {}", name);
                } else {
                    name = "NaverWorks User";
                    log.warn("네이버웍스에서 name 정보 없음 - 기본값 사용");
                }
                // userInfo 맵에도 기본값 반영 (createMemberFromNaverWorks에서 사용)
                userInfo.put("name", name);
            }

            log.info("사용할 이름: {}", name);

            // 3. 사용자 처리
            if (member == null) {
                // 신규 등록
                log.info("신규 사용자 생성 시작");
                member = createMemberFromNaverWorks(userInfo);
                if (member == null) {
                    log.error("신규 사용자 생성 실패 - createMemberFromNaverWorks returned null");
                    return null;
                }
                log.info("신규 사용자 생성 완료: ID={}", member.getId());
            } else {
                // 기존 사용자 정보 업데이트
                log.info("기존 사용자 정보 업데이트 시작: ID={}", member.getId());

                // 정보 업데이트
                log.info("[{}] 업데이트할 departmentId: {}", email, departmentId);
                member.setName(name);
                member.setPosition(position);
                member.setPhone(phone);
                member.setDepartmentId(departmentId);
                member.setNaverworksUserId(naverworksUserId);
                member.setLastLoginAt(LocalDateTime.now());

                // DB 업데이트
                memberMapper.updateNaverWorksLoginInfo(member);

                // 최신 정보 재조회
                member = memberMapper.findById(member.getId());

                log.info("사용자 정보 업데이트 완료");
            }

            return member;

        } catch (Exception e) {
            log.error("사용자 Upsert 중 예외 발생", e);
            return null;
        }
    }

    /**
     * 네이버웍스 사용자 정보로 새 회원 등록
     * (name은 upsertMember에서 이미 기본값 처리됨)
     */
    private MemberIntranet createMemberFromNaverWorks(Map<String, Object> userInfo) {
        try {
            log.info("신규 회원 등록 시작");
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String naverworksUserId = (String) userInfo.get("userId");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");  // upsertMember에서 이미 기본값 설정됨
            String phone = (String) userInfo.get("mobilePhone");
            if (phone == null || phone.isEmpty()) {
                phone = (String) userInfo.get("telephoneNumber");
            }
            String position = (String) userInfo.get("position");
            Long departmentId = (Long) userInfo.get("departmentId");

            log.info("신규 회원 정보 - 이메일: {}, 이름: {}, 부서ID: {}, 직급: {}", email, name, departmentId, position);

            // 기본 비밀번호 생성 (1234)
            String defaultPassword = "1234";
            String encodedPassword = passwordEncoder.encode(defaultPassword);

            MemberIntranet newMember = MemberIntranet.builder()
                    .email(email)
                    .password(encodedPassword)
                    .name(name)
                    .phone(phone)
                    .departmentId(departmentId)  // 네이버웍스에서 가져온 부서 ID
                    .position(position)
                    .role("USER")  // 기본 권한
                    .hireDate(LocalDate.now())  // 오늘 날짜
                    .annualLeaveGranted(BigDecimal.valueOf(15))  // 기본 15일
                    .isActive(true)
                    .naverworksUserId(naverworksUserId)  // 네이버웍스 고유 ID
                    .lastLoginAt(LocalDateTime.now())  // 최초 로그인 시간
                    .build();

            log.info("DB에 사용자 저장 시도");
            MemberIntranet created = memberService.createMember(newMember);
            log.info("신규 회원 등록 완료: ID={}, 이메일={}", created.getId(), created.getEmail());

            return created;

        } catch (Exception e) {
            log.error("신규 회원 등록 중 예외 발생", e);
            return null;
        }
    }
}
