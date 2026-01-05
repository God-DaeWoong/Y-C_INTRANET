package com.ync.intranet.controller;

import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.service.MemberIntranetService;
import com.ync.intranet.service.NaverWorksOAuthService;
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

        // 네이버웍스 인증 페이지로 리다이렉트
        String authUrl = naverWorksOAuthService.getAuthorizationUrl(state);
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
            HttpServletResponse response
    ) throws IOException {
        try {
            // State 검증 (CSRF 방지)
            String savedState = (String) session.getAttribute("oauth_state");
            if (savedState == null || !savedState.equals(state)) {
                response.sendRedirect("/intranet-login.html?error=invalid_state");
                return;
            }

            // 1. Access Token 발급
            String accessToken = naverWorksOAuthService.getAccessToken(code);

            // 2. 사용자 정보 조회
            Map<String, Object> userInfo = naverWorksOAuthService.getUserInfo(accessToken);
            String email = (String) userInfo.get("email");

            if (email == null || email.isEmpty()) {
                response.sendRedirect("/intranet-login.html?error=no_email");
                return;
            }

            // 3. DB에서 사용자 조회
            MemberIntranet member = memberService.findByEmail(email);

            // 사용자가 없으면 자동 등록
            if (member == null) {
                member = createMemberFromNaverWorks(userInfo);
                if (member == null) {
                    response.sendRedirect("/intranet-login.html?error=user_creation_failed");
                    return;
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

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
