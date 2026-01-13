package com.ync.intranet.domain;

import java.time.LocalDateTime;

/**
 * 알림 정보 (인트라넷)
 */
public class NotificationIntranet {

    private Long id;
    private Long memberId;
    private String notificationType;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public NotificationIntranet() {
    }

    public NotificationIntranet(Long id, Long memberId, String notificationType, String title,
                                String content, String linkUrl, Boolean isRead,
                                LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.memberId = memberId;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.linkUrl = linkUrl;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public static NotificationIntranetBuilder builder() {
        return new NotificationIntranetBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
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

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    // Notification Types
    public static class NotificationType {
        public static final String APPROVAL_REQUEST = "APPROVAL_REQUEST";      // 결재 요청
        public static final String APPROVAL_APPROVED = "APPROVAL_APPROVED";    // 결재 승인됨
        public static final String APPROVAL_REJECTED = "APPROVAL_REJECTED";    // 결재 반려됨
        public static final String DOCUMENT_COMMENT = "DOCUMENT_COMMENT";      // 문서 댓글
        public static final String SCHEDULE_REMINDER = "SCHEDULE_REMINDER";    // 일정 알림
        public static final String LEAVE_APPROVED = "LEAVE_APPROVED";          // 휴가 승인됨
        public static final String LEAVE_REJECTED = "LEAVE_REJECTED";          // 휴가 반려됨
        public static final String ANNOUNCEMENT = "ANNOUNCEMENT";              // 공지사항
        public static final String MENTION = "MENTION";                        // 멘션
    }

    // Builder
    public static class NotificationIntranetBuilder {
        private Long id;
        private Long memberId;
        private String notificationType;
        private String title;
        private String content;
        private String linkUrl;
        private Boolean isRead;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;

        NotificationIntranetBuilder() {
        }

        public NotificationIntranetBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public NotificationIntranetBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public NotificationIntranetBuilder notificationType(String notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public NotificationIntranetBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationIntranetBuilder content(String content) {
            this.content = content;
            return this;
        }

        public NotificationIntranetBuilder linkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
            return this;
        }

        public NotificationIntranetBuilder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public NotificationIntranetBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationIntranetBuilder readAt(LocalDateTime readAt) {
            this.readAt = readAt;
            return this;
        }

        public NotificationIntranet build() {
            return new NotificationIntranet(id, memberId, notificationType, title, content,
                    linkUrl, isRead, createdAt, readAt);
        }
    }
}
