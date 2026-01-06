package com.ync.intranet.controller;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.service.ApprovalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 결재 컨트롤러 (인트라넷)
 */
@RestController("approvalControllerIntranet")
@RequestMapping("/api/intranet/approvals")
@CrossOrigin(origins = "*")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * 내 대기중인 결재 목록
     * GET /api/intranet/approvals/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        List<ApprovalLineIntranet> approvals = approvalService.getPendingApprovals(userId);
        return ResponseEntity.ok(Map.of("success", true, "approvals", approvals));
    }

    /**
     * 결재 상세 조회 (ID로)
     * GET /api/intranet/approvals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApprovalDetail(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            ApprovalLineIntranet approval = approvalService.getApprovalById(id);
            if (approval == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "결재를 찾을 수 없습니다."));
            }

            // 결재 권한 확인 (본인 결재건이거나 본인이 작성한 문서인지)
            boolean isApprover = approval.getApproverId().equals(userId);
            boolean isAuthor = approval.getDocument() != null &&
                             approval.getDocument().getAuthorId().equals(userId);

            if (!isApprover && !isAuthor) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            return ResponseEntity.ok(Map.of("success", true, "approval", approval));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 내 모든 결재 목록
     * GET /api/intranet/approvals/my
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyApprovals(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        List<ApprovalLineIntranet> approvals = approvalService.getApprovalsByApproverId(userId);
        return ResponseEntity.ok(Map.of("success", true, "approvals", approvals));
    }

    /**
     * 문서의 결재선 조회
     * GET /api/intranet/approvals/document/{documentId}
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<ApprovalLineIntranet>> getDocumentApprovals(@PathVariable Long documentId) {
        return ResponseEntity.ok(approvalService.getDocumentApprovals(documentId));
    }

    /**
     * 결재 승인
     * POST /api/intranet/approvals/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id,
                                                        @RequestBody Map<String, String> request,
                                                        HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            String comment = request.get("comment");
            approvalService.approve(id, userId, comment);

            return ResponseEntity.ok(Map.of("success", true, "message", "승인되었습니다."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 결재 반려
     * POST /api/intranet/approvals/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id,
                                                       @RequestBody Map<String, String> request,
                                                       HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            String comment = request.get("comment");
            approvalService.reject(id, userId, comment);

            return ResponseEntity.ok(Map.of("success", true, "message", "반려되었습니다."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 결재 취소 (작성자)
     * POST /api/intranet/approvals/document/{documentId}/cancel
     */
    @PostMapping("/document/{documentId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApproval(@PathVariable Long documentId,
                                                               HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            approvalService.cancelApproval(documentId, userId);

            return ResponseEntity.ok(Map.of("success", true, "message", "결재가 취소되었습니다."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 완료된 결재 목록 조회 (승인/반려 완료)
     * GET /api/intranet/approvals/completed
     */
    @GetMapping("/completed")
    public ResponseEntity<Map<String, Object>> getCompletedApprovals(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            List<ApprovalLineIntranet> approvals = approvalService.getCompletedApprovals(userId, title, startDate, endDate);
            return ResponseEntity.ok(Map.of("success", true, "approvals", approvals));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "조회 중 오류가 발생했습니다."));
        }
    }
}
