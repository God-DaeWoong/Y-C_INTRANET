package com.ync.intranet.controller;

import com.ync.intranet.domain.AttachmentIntranet;
import com.ync.intranet.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 첨부파일 API Controller (인트라넷)
 */
@RestController
@RequestMapping("/api/intranet/attachments")
@CrossOrigin(origins = "*")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * 파일 업로드
     * POST /api/intranet/attachments/upload
     *
     * @param file 업로드할 파일
     * @param documentId 문서 ID
     * @param session HTTP 세션
     * @return 업로드된 첨부파일 정보
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentId") Long documentId,
            HttpSession session) {

        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다");
            }

            // 파일 업로드
            AttachmentIntranet attachment = attachmentService.uploadFile(file, documentId, userId);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("id", attachment.getId());
            response.put("fileName", attachment.getFileName());
            response.put("fileSize", attachment.getFileSize());
            response.put("fileSizeFormatted", AttachmentService.formatFileSize(attachment.getFileSize()));
            response.put("uploadedAt", attachment.getUploadedAt());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * 파일 다운로드
     * GET /api/intranet/attachments/download/{id}
     *
     * @param id 첨부파일 ID
     * @return 파일 리소스
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {

        try {
            // 첨부파일 정보 조회
            AttachmentIntranet attachment = attachmentService.getAttachmentById(id);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            // 파일 리소스 가져오기
            Resource resource = attachmentService.downloadFile(id);

            // 파일명 UTF-8 인코딩 (한글 파일명 지원)
            String encodedFilename = new String(
                    attachment.getFileName().getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.ISO_8859_1
            );

            // 응답 헤더 설정
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * 문서의 첨부파일 목록 조회
     * GET /api/intranet/attachments/document/{documentId}
     *
     * @param documentId 문서 ID
     * @return 첨부파일 목록
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<?> getAttachmentsByDocumentId(@PathVariable Long documentId) {

        try {
            List<AttachmentIntranet> attachments = attachmentService.getAttachmentsByDocumentId(documentId);

            // 파일 크기 포맷팅 추가
            List<Map<String, Object>> response = attachments.stream()
                    .map(attachment -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", attachment.getId());
                        map.put("fileName", attachment.getFileName());
                        map.put("fileSize", attachment.getFileSize());
                        map.put("fileSizeFormatted", AttachmentService.formatFileSize(attachment.getFileSize()));
                        map.put("fileType", attachment.getFileType());
                        map.put("uploadedAt", attachment.getUploadedAt());
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("첨부파일 목록 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 첨부파일 삭제
     * DELETE /api/intranet/attachments/{id}
     *
     * @param id 첨부파일 ID
     * @param session HTTP 세션
     * @return 성공 여부
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id, HttpSession session) {

        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다");
            }

            // 첨부파일 삭제
            attachmentService.deleteAttachment(id);

            return ResponseEntity.ok("첨부파일이 삭제되었습니다");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("첨부파일 삭제 중 오류가 발생했습니다");
        }
    }
}
