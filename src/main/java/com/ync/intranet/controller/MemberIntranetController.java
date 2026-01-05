package com.ync.intranet.controller;

import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.service.MemberIntranetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 사원 관리 컨트롤러 (인트라넷)
 */
@RestController("memberIntranetController")
@RequestMapping("/api/intranet/members")
@CrossOrigin(origins = "*")
public class MemberIntranetController {

    private final MemberIntranetService memberService;

    public MemberIntranetController(MemberIntranetService memberService) {
        this.memberService = memberService;
    }

    /**
     * 전체 사원 조회
     * GET /api/intranet/members
     */
    @GetMapping
    public ResponseEntity<List<MemberIntranet>> getAllMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }

    /**
     * 활성화된 사원만 조회
     * GET /api/intranet/members/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<MemberIntranet>> getActiveMembers() {
        return ResponseEntity.ok(memberService.findActiveMembers());
    }

    /**
     * ID로 사원 조회
     * GET /api/intranet/members/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberIntranet> getMember(@PathVariable Long id) {
        MemberIntranet member = memberService.findById(id);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(member);
    }

    /**
     * 부서별 사원 조회
     * GET /api/intranet/members/department/{departmentId}
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<MemberIntranet>> getMembersByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(memberService.findByDepartmentId(departmentId));
    }

    /**
     * 권한별 사원 조회
     * GET /api/intranet/members/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<MemberIntranet>> getMembersByRole(@PathVariable String role) {
        return ResponseEntity.ok(memberService.findByRole(role));
    }

    /**
     * 사원 등록 (ADMIN 전용)
     * POST /api/intranet/members
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMember(@RequestBody MemberIntranet member,
                                                             HttpSession session) {
        try {
            // 권한 확인
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            MemberIntranet created = memberService.createMember(member);
            return ResponseEntity.ok(Map.of("success", true, "member", created));

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "사원 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사원 수정 (ADMIN 전용)
     * PUT /api/intranet/members/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMember(@PathVariable Long id,
                                                             @RequestBody MemberIntranet member,
                                                             HttpSession session) {
        try {
            // 권한 확인
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            member.setId(id);
            MemberIntranet updated = memberService.updateMember(member);
            return ResponseEntity.ok(Map.of("success", true, "member", updated));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 사원 비활성화 (ADMIN 전용)
     * POST /api/intranet/members/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateMember(@PathVariable Long id,
                                                                 HttpSession session) {
        try {
            // 권한 확인
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            memberService.deactivateMember(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "사원이 비활성화되었습니다."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
