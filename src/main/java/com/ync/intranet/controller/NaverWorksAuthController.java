package com.ync.intranet.controller;

import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.service.MemberIntranetService;
import com.ync.intranet.service.NaverWorksOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * 네이버웍스 OAuth 인증 컨트롤러
 */
@RestController
@RequestMapping("/api/intranet/auth/naver-works")
@CrossOrigin(origins = "*")
public class NaverWorksAuthController {

    private final NaverWorksOAuthService naverWorksOAuthService;
    private final MemberIntranetService memberService;

    public NaverWorksAuthController(NaverWorksOAuthService naverWorksOAuthService,
                                    MemberIntranetService memberService) {
        this.naverWorksOAuthService = naverWorksOAuthService;
        this.memberService = memberService;
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
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {
        try {
            System.out.println("📞 네이버웍스 콜백 수신");
            System.out.println("  - Session ID: " + session.getId());
            System.out.println("  - Code: " + (code != null ? "존재함" : "없음"));

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

            // 3. DB에서 사용자 조회
            MemberIntranet member = memberService.findByEmail(email);
            System.out.println("🔍 DB 사용자 조회 결과: " + (member != null ? "존재함" : "없음"));

            // 사용자가 없으면 자동 등록
            if (member == null) {
                System.out.println("🆕 신규 사용자 생성 시작...");
                try {
                    member = createMemberFromNaverWorks(userInfo);
                    if (member == null) {
                        System.err.println("❌ 사용자 생성 실패 (null 반환)");
                        response.sendRedirect("/intranet-login.html?error=user_creation_failed");
                        return;
                    }
                    System.out.println("✅ 사용자 생성 성공: " + member.getEmail());
                } catch (RuntimeException e) {
                    // 중복 생성 시도 등 예외 처리
                    System.err.println("⚠️ 사용자 생성 중 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                    member = memberService.findByEmail(email);
                    if (member == null) {
                        System.err.println("❌ 재조회 실패");
                        response.sendRedirect("/intranet-login.html?error=user_creation_failed");
                        return;
                    }
                    System.out.println("✅ 재조회 성공: " + member.getEmail());
                }
            }

            if (!member.getIsActive()) {
                response.sendRedirect("/intranet-login.html?error=user_inactive");
                return;
            }

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
     * 네이버웍스 사용자 정보로 새 회원 등록
     */
    private MemberIntranet createMemberFromNaverWorks(Map<String, Object> userInfo) {
        try {
            System.out.println("🔧 createMemberFromNaverWorks 시작");
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String phone = (String) userInfo.get("mobilePhone");
            if (phone == null || phone.isEmpty()) {
                phone = (String) userInfo.get("telephoneNumber");
            }
            String position = (String) userInfo.get("position");

            System.out.println("  - Email: " + email);
            System.out.println("  - Name: " + name);
            System.out.println("  - Phone: " + phone);
            System.out.println("  - Position: " + position);

            // 기본 비밀번호 생성 (1234)
            String defaultPassword = "1234";
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

            System.out.println("💾 DB에 사용자 저장 시도...");
            MemberIntranet created = memberService.createMember(newMember);
            System.out.println("✅ 사용자 저장 완료: ID=" + created.getId());
            return created;

        } catch (Exception e) {
            System.err.println("❌ createMemberFromNaverWorks 예외 발생:");
            e.printStackTrace();
            return null;
        }
    }
}
