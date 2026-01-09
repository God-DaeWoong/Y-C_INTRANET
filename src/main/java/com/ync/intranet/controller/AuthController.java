package com.ync.intranet.controller;

import com.ync.intranet.domain.DepartmentIntranet;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.mapper.DepartmentIntranetMapper;
import com.ync.intranet.mapper.MemberIntranetMapper;
import com.ync.intranet.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 컨트롤러 (인트라넷)
 * Session 기반 로그인/로그아웃
 */
@RestController("authControllerIntranet")
@RequestMapping("/api/intranet/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final MemberIntranetMapper memberMapper;
    private final DepartmentIntranetMapper departmentMapper;

    public AuthController(AuthService authService, MemberIntranetMapper memberMapper,
                          DepartmentIntranetMapper departmentMapper) {
        this.authService = authService;
        this.memberMapper = memberMapper;
        this.departmentMapper = departmentMapper;
    }

    /**
     * 로그인
     * POST /api/intranet/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest,
                                                      HttpSession session) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이메일과 비밀번호를 입력해주세요."));
            }

            // 로그인 처리
            MemberIntranet member = authService.login(email, password);

            // 세션에 사용자 정보 저장
            session.setAttribute("userId", member.getId());
            session.setAttribute("userEmail", member.getEmail());
            session.setAttribute("userName", member.getName());
            session.setAttribute("userRole", member.getRole());

            // 응답
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", buildUserMap(member));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() + "\n\n네이버웍스 로그인을 통해 사용자 정보를 생성할 수 있습니다.";
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", errorMessage));
        }
    }

    /**
     * 로그아웃
     * POST /api/intranet/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃되었습니다."));
    }

    /**
     * 현재 로그인 사용자 정보 조회
     * GET /api/intranet/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("email", session.getAttribute("userEmail"));
        user.put("name", session.getAttribute("userName"));
        user.put("role", session.getAttribute("userRole"));

        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    /**
     * 세션 체크 (인증 여부 확인)
     * GET /api/intranet/auth/session
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("authenticated", false, "message", "로그인이 필요합니다."));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", userId,
                "userName", session.getAttribute("userName")
        ));
    }

    /**
     * 비밀번호 변경
     * POST /api/intranet/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request,
                                                               HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "기존 비밀번호와 새 비밀번호를 입력해주세요."));
            }

            authService.changePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 변경되었습니다."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 이메일만으로 로그인 (비밀번호 검증 없음 - 임시)
     * POST /api/intranet/auth/email-only
     * TODO: 네이버웍스 OAuth 연동 후 이 엔드포인트 제거
     */
    @PostMapping("/email-only")
    public ResponseEntity<Map<String, Object>> loginWithEmailOnly(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이메일을 입력해주세요."));
            }

            // 네이버웍스 도메인 확인
            if (!email.endsWith("@yncsmart.com")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "회사 이메일(@yncsmart.com)만 사용 가능합니다."));
            }

            // DB에서 사용자 찾기
            MemberIntranet member = memberMapper.findByEmail(email);

            if (member == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "등록되지 않은 사용자입니다."));
            }

            if (!member.getIsActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비활성화된 계정입니다."));
            }

            // 세션 생성 (비밀번호 검증 없이 바로 로그인)
            session.setAttribute("userId", member.getId());
            session.setAttribute("userEmail", member.getEmail());
            session.setAttribute("userName", member.getName());
            session.setAttribute("userRole", member.getRole());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그인 성공",
                    "user", buildUserMap(member)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인 실패: " + e.getMessage()));
        }
    }

    /**
     * 네이버웍스 이메일 로그인 (Option 2)
     * POST /api/intranet/auth/naver-works-email
     */
    @PostMapping("/naver-works-email")
    public ResponseEntity<Map<String, Object>> loginWithNaverWorksEmail(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이메일과 비밀번호를 입력해주세요."));
            }

            // 네이버웍스 도메인 확인
            if (!email.endsWith("@yncsmart.com")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "회사 이메일(@yncsmart.com)만 사용 가능합니다."));
            }

            // DB에서 사용자 찾기 (비밀번호 포함)
            MemberIntranet member = memberMapper.findByEmail(email);

            if (member == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "등록되지 않은 사용자입니다."));
            }

            if (!member.getIsActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비활성화된 계정입니다."));
            }

            // 비밀번호 확인
            String hashedPassword = member.getPassword();

            if (hashedPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비밀번호가 설정되지 않았습니다."));
            }

            if (!authService.checkPassword(password, hashedPassword)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "비밀번호가 일치하지 않습니다."));
            }

            // 세션 생성
            session.setAttribute("userId", member.getId());
            session.setAttribute("userEmail", member.getEmail());
            session.setAttribute("userName", member.getName());
            session.setAttribute("userRole", member.getRole());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "네이버웍스 로그인 성공",
                    "user", buildUserMap(member)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인 실패: " + e.getMessage()));
        }
    }

    /**
     * 결재자 조회 (조직도 기반)
     * GET /api/intranet/auth/approver
     */
    @GetMapping("/approver")
    public ResponseEntity<Map<String, Object>> getApprover(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Map<String, Object> approverInfo = authService.getApproverForUser(userId);

            if (approverInfo == null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "approver", Map.of(),
                        "message", "결재자를 찾을 수 없습니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "approver", approverInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "결재자 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 결재 가능한 사용자 목록 조회
     * GET /api/intranet/auth/approvers
     */
    @GetMapping("/approvers")
    public ResponseEntity<Map<String, Object>> getApproverList(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "approvers", authService.getApproverList()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "결재자 목록 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자 정보 Map 생성 (부서 정보 포함)
     */
    private Map<String, Object> buildUserMap(MemberIntranet member) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", member.getId());
        userMap.put("email", member.getEmail());
        userMap.put("name", member.getName());
        userMap.put("role", member.getRole());
        userMap.put("position", member.getPosition() != null ? member.getPosition() : "");
        userMap.put("departmentId", member.getDepartmentId());
        userMap.put("departmentName", member.getDepartmentName() != null ? member.getDepartmentName() : "");

        // 부서의 parent department ID 조회
        if (member.getDepartmentId() != null) {
            DepartmentIntranet dept = departmentMapper.findById(member.getDepartmentId());
            if (dept != null && dept.getParentId() != null) {
                userMap.put("parentDepartmentId", dept.getParentId());
            }
        }

        return userMap;
    }
}
