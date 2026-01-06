package com.ync.intranet.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문서 통합 테이블 (인트라넷)
 * 휴가, 경비보고서, 일반 문서 모두 포함
 */
public class DocumentIntranet {

    private Long id;
    private DocumentType documentType;
    private Long authorId;
    private String authorName;  // JOIN용
    private String authorPosition;  // JOIN용 (작성자 직급)
    private String authorDepartment;  // JOIN용 (작성자 부서)
    private String title;
    private String content;
    private DocumentStatus status;
    private String metadata;  // JSON 형식
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;

    // 연관 객체
    private MemberIntranet author;
    private List<ApprovalLineIntranet> approvalLines;

    public DocumentIntranet() {
    }

    public DocumentIntranet(Long id, DocumentType documentType, Long authorId, String authorName,
                            String title, String content, DocumentStatus status, String metadata,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            LocalDateTime submittedAt, LocalDateTime approvedAt) {
        this.id = id;
        this.documentType = documentType;
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.status = status;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
    }

    public static DocumentIntranetBuilder builder() {
        return new DocumentIntranetBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorPosition() {
        return authorPosition;
    }

    public void setAuthorPosition(String authorPosition) {
        this.authorPosition = authorPosition;
    }

    public String getAuthorDepartment() {
        return authorDepartment;
    }

    public void setAuthorDepartment(String authorDepartment) {
        this.authorDepartment = authorDepartment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public MemberIntranet getAuthor() {
        return author;
    }

    public void setAuthor(MemberIntranet author) {
        this.author = author;
    }

    public List<ApprovalLineIntranet> getApprovalLines() {
        return approvalLines;
    }

    public void setApprovalLines(List<ApprovalLineIntranet> approvalLines) {
        this.approvalLines = approvalLines;
    }

    // Enums
    public enum DocumentType {
        LEAVE, EXPENSE, GENERAL
    }

    public enum DocumentStatus {
        DRAFT, PENDING, APPROVED, REJECTED, CANCELED
    }

    // Builder
    public static class DocumentIntranetBuilder {
        private Long id;
        private DocumentType documentType;
        private Long authorId;
        private String authorName;
        private String authorPosition;
        private String authorDepartment;
        private String title;
        private String content;
        private DocumentStatus status;
        private String metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime submittedAt;
        private LocalDateTime approvedAt;

        DocumentIntranetBuilder() {
        }

        public DocumentIntranetBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DocumentIntranetBuilder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public DocumentIntranetBuilder authorId(Long authorId) {
            this.authorId = authorId;
            return this;
        }

        public DocumentIntranetBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public DocumentIntranetBuilder authorPosition(String authorPosition) {
            this.authorPosition = authorPosition;
            return this;
        }

        public DocumentIntranetBuilder authorDepartment(String authorDepartment) {
            this.authorDepartment = authorDepartment;
            return this;
        }

        public DocumentIntranetBuilder title(String title) {
            this.title = title;
            return this;
        }

        public DocumentIntranetBuilder content(String content) {
            this.content = content;
            return this;
        }

        public DocumentIntranetBuilder status(DocumentStatus status) {
            this.status = status;
            return this;
        }

        public DocumentIntranetBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public DocumentIntranetBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DocumentIntranetBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DocumentIntranetBuilder submittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public DocumentIntranetBuilder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public DocumentIntranet build() {
            return new DocumentIntranet(id, documentType, authorId, authorName, title, content,
                    status, metadata, createdAt, updatedAt, submittedAt, approvedAt);
        }
    }
}
