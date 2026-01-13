package com.ync.intranet.domain;

import java.time.LocalDateTime;

/**
 * 첨부파일 Domain (인트라넷)
 */
public class AttachmentIntranet {

    private Long id;
    private Long documentId;           // 문서 ID
    private String fileName;           // 파일명 (UUID + 원본파일명)
    private String filePath;           // 파일 저장 경로
    private Long fileSize;             // 파일 크기 (bytes)
    private String fileType;           // 파일 타입 (MIME type)
    private Long uploadedBy;           // 업로드한 사용자 ID
    private LocalDateTime uploadedAt;  // 업로드 일시

    // 기본 생성자
    public AttachmentIntranet() {
    }

    // 전체 필드 생성자
    public AttachmentIntranet(Long id, Long documentId, String fileName, String filePath,
                              Long fileSize, String fileType, Long uploadedBy, LocalDateTime uploadedAt) {
        this.id = id;
        this.documentId = documentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Builder 패턴
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long documentId;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String fileType;
        private Long uploadedBy;
        private LocalDateTime uploadedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder uploadedBy(Long uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }

        public Builder uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        public AttachmentIntranet build() {
            return new AttachmentIntranet(id, documentId, fileName, filePath,
                                          fileSize, fileType, uploadedBy, uploadedAt);
        }
    }
}
