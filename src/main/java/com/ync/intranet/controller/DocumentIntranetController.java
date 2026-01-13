package com.ync.intranet.controller;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.domain.AttachmentIntranet;
import com.ync.intranet.domain.DocumentIntranet;
import com.ync.intranet.service.ApprovalService;
import com.ync.intranet.service.AttachmentService;
import com.ync.intranet.service.DocumentIntranetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 문서 관리 컨트롤러 (인트라넷)
 */
@RestController
@RequestMapping("/api/intranet/documents")
public class DocumentIntranetController {

    private final DocumentIntranetService documentService;
    private final ApprovalService approvalService;
    private final AttachmentService attachmentService;

    public DocumentIntranetController(DocumentIntranetService documentService,
                                     ApprovalService approvalService,
                                     AttachmentService attachmentService) {
        this.documentService = documentService;
        this.approvalService = approvalService;
        this.attachmentService = attachmentService;
    }

    /**
     * 문서 생성 및 결재 상신
     * POST /api/intranet/documents
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDocument(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 문서 정보 추출
            String title = (String) request.get("title");
            String documentType = (String) request.get("documentType");
            String content = (String) request.get("content");
            @SuppressWarnings("unchecked")
            List<Object> approverIdsRaw = (List<Object>) request.get("approverIds");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "제목을 입력해주세요."));
            }

            if (approverIdsRaw == null || approverIdsRaw.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "결재자를 선택해주세요."));
            }

            // Object를 Long으로 변환
            List<Long> approverIds = new ArrayList<>();
            for (Object obj : approverIdsRaw) {
                if (obj instanceof Number) {
                    approverIds.add(((Number) obj).longValue());
                } else if (obj instanceof String) {
                    approverIds.add(Long.parseLong((String) obj));
                }
            }

            // 1. 문서 생성
            DocumentIntranet document = new DocumentIntranet();
            document.setAuthorId(userId);
            document.setTitle(title);
            document.setContent(content != null ? content : "");

            // 문서 타입 설정
            if ("EXPENSE".equals(documentType)) {
                document.setDocumentType(DocumentIntranet.DocumentType.EXPENSE);
            } else if ("LEAVE".equals(documentType) || "VACATION".equals(documentType)) {
                document.setDocumentType(DocumentIntranet.DocumentType.LEAVE);

                // 휴가신청서인 경우 메타데이터 설정
                String leaveType = (String) request.get("leaveType");
                String halfDayType = (String) request.get("halfDayType");

                if (leaveType != null) {
                    StringBuilder metadata = new StringBuilder("{\"leaveType\":\"" + leaveType + "\"");
                    if ("HALF_DAY".equals(leaveType) && halfDayType != null) {
                        metadata.append(",\"halfDayType\":\"").append(halfDayType).append("\"");
                    }
                    metadata.append("}");
                    document.setMetadata(metadata.toString());
                }
            } else {
                document.setDocumentType(DocumentIntranet.DocumentType.GENERAL);
            }

            DocumentIntranet createdDocument = documentService.createDocument(document);

            // 2. 결재선 생성
            List<ApprovalLineIntranet> approvalLines = new ArrayList<>();
            for (Long approverId : approverIds) {
                ApprovalLineIntranet line = new ApprovalLineIntranet();
                line.setApproverId(approverId);
                approvalLines.add(line);
            }

            // 3. 결재 상신
            documentService.submitDocument(createdDocument.getId(), approvalLines);

            // 4. 휴가신청서인 경우 일정도 생성 (VACATION 또는 LEAVE 타입 모두 허용)
            if ("VACATION".equals(documentType) || "LEAVE".equals(documentType) || "VACATION_REQUEST".equals(documentType)) {
                System.out.println("[문서 생성] 휴가신청서 일정 생성 시작: documentType=" + documentType);
                approvalService.createScheduleFromVacationDocument(createdDocument);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "문서가 성공적으로 상신되었습니다.",
                    "documentId", createdDocument.getId()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "문서 상신에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 문서 상세 조회
     * GET /api/intranet/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDocument(
            @PathVariable Long id,
            HttpSession session
    ) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            DocumentIntranet document = documentService.findById(id);
            if (document == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "문서를 찾을 수 없습니다."));
            }

            // 첨부파일 목록 조회
            List<AttachmentIntranet> attachments = attachmentService.getAttachmentsByDocumentId(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "document", document,
                    "attachments", attachments
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "문서 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 내가 작성한 문서 목록 조회
     * GET /api/intranet/documents/my
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyDocuments(
            @RequestParam(required = false) String status,
            HttpSession session
    ) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            List<DocumentIntranet> documents;
            if (status != null && !status.isEmpty()) {
                documents = documentService.findByAuthorIdAndStatus(userId, status);
            } else {
                documents = documentService.findByAuthorId(userId);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "documents", documents
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "문서 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 문서 삭제
     * DELETE /api/intranet/documents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long id,
            HttpSession session
    ) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            documentService.deleteDocument(id, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "문서가 삭제되었습니다."
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "문서 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
}
