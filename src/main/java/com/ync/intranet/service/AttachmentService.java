package com.ync.intranet.service;

import com.ync.intranet.domain.AttachmentIntranet;
import com.ync.intranet.mapper.AttachmentIntranetMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 첨부파일 서비스 (인트라넷)
 */
@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentIntranetMapper attachmentMapper;

    // 파일 업로드 디렉토리 (실제 환경에 맞게 수정 필요)
    private static final String UPLOAD_DIR = "C:/uploads/intranet";

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "zip", "txt"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public AttachmentService(AttachmentIntranetMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;

        // 업로드 디렉토리 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param documentId 문서 ID
     * @param userId 업로드 사용자 ID
     * @return 저장된 첨부파일 정보
     */
    @Transactional
    public AttachmentIntranet uploadFile(MultipartFile file, Long documentId, Long userId) throws IOException {
        // 1. 파일 검증
        validateFile(file);

        // 2. 저장 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String filePath = UPLOAD_DIR + File.separator + fileName;

        // 3. 파일 저장
        File destFile = new File(filePath);
        file.transferTo(destFile);

        // 4. DB에 메타데이터 저장
        AttachmentIntranet attachment = AttachmentIntranet.builder()
                .documentId(documentId)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploadedBy(userId)
                .build();

        attachmentMapper.insert(attachment);

        return attachment;
    }

    /**
     * 파일 다운로드 (Resource 반환)
     * @param attachmentId 첨부파일 ID
     * @return 파일 리소스
     */
    public Resource downloadFile(Long attachmentId) throws IOException {
        AttachmentIntranet attachment = attachmentMapper.findById(attachmentId);

        if (attachment == null) {
            throw new RuntimeException("첨부파일을 찾을 수 없습니다: " + attachmentId);
        }

        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다: " + attachment.getFileName());
        }

        return resource;
    }

    /**
     * 첨부파일 정보 조회
     * @param attachmentId 첨부파일 ID
     * @return 첨부파일 정보
     */
    public AttachmentIntranet getAttachmentById(Long attachmentId) {
        return attachmentMapper.findById(attachmentId);
    }

    /**
     * 문서의 첨부파일 목록 조회
     * @param documentId 문서 ID
     * @return 첨부파일 목록
     */
    public List<AttachmentIntranet> getAttachmentsByDocumentId(Long documentId) {
        return attachmentMapper.findByDocumentId(documentId);
    }

    /**
     * 첨부파일 삭제
     * @param attachmentId 첨부파일 ID
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        AttachmentIntranet attachment = attachmentMapper.findById(attachmentId);

        if (attachment != null) {
            // 1. 물리 파일 삭제
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                file.delete();
            }

            // 2. DB 레코드 삭제
            attachmentMapper.deleteById(attachmentId);
        }
    }

    /**
     * 문서의 모든 첨부파일 삭제
     * @param documentId 문서 ID
     */
    @Transactional
    public void deleteAttachmentsByDocumentId(Long documentId) {
        List<AttachmentIntranet> attachments = attachmentMapper.findByDocumentId(documentId);

        for (AttachmentIntranet attachment : attachments) {
            // 물리 파일 삭제
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        // DB 레코드 삭제
        attachmentMapper.deleteByDocumentId(documentId);
    }

    /**
     * 파일 검증
     * @param file 업로드할 파일
     */
    private void validateFile(MultipartFile file) {
        // 1. 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다");
        }

        // 2. 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 3. 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("올바르지 않은 파일명입니다");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("허용되지 않은 파일 형식입니다: " + extension);
        }
    }

    /**
     * 파일 크기 포맷팅 (bytes → KB/MB)
     * @param bytes 파일 크기 (bytes)
     * @return 포맷된 문자열
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
