package com.ync.intranet.service;

import com.ync.intranet.domain.NotificationIntranet;
import com.ync.intranet.mapper.NotificationIntranetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 (인트라넷)
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationIntranetMapper notificationMapper;

    public NotificationService(NotificationIntranetMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    /**
     * 결재 요청 알림 생성
     * @param approverId 결재자 ID
     * @param requesterName 요청자 이름
     * @param documentTitle 문서 제목
     * @param documentId 문서 ID
     */
    public void createApprovalRequestNotification(Long approverId, String requesterName, String documentTitle, Long documentId) {
        try {
            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(approverId)
                    .notificationType(NotificationIntranet.NotificationType.APPROVAL_REQUEST)
                    .title("새로운 결재 요청")
                    .content(requesterName + "님이 결재를 요청했습니다: " + documentTitle)
                    .linkUrl("/approval-pending.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("결재 요청 알림 생성 완료 - 결재자: {}, 문서: {}", approverId, documentTitle);
        } catch (Exception e) {
            log.error("결재 요청 알림 생성 실패 - 결재자: {}, 문서: {}", approverId, documentTitle, e);
        }
    }

    /**
     * 결재 승인 알림 생성
     * @param requesterId 기안자 ID
     * @param approverName 결재자 이름
     * @param documentTitle 문서 제목
     * @param documentId 문서 ID
     */
    public void createApprovalApprovedNotification(Long requesterId, String approverName, String documentTitle, Long documentId) {
        try {
            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(requesterId)
                    .notificationType(NotificationIntranet.NotificationType.APPROVAL_APPROVED)
                    .title("결재 승인")
                    .content(approverName + "님이 결재를 승인했습니다: " + documentTitle)
                    .linkUrl("/my-documents.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("결재 승인 알림 생성 완료 - 기안자: {}, 문서: {}", requesterId, documentTitle);
        } catch (Exception e) {
            log.error("결재 승인 알림 생성 실패 - 기안자: {}, 문서: {}", requesterId, documentTitle, e);
        }
    }

    /**
     * 결재 반려 알림 생성
     * @param requesterId 기안자 ID
     * @param approverName 결재자 이름
     * @param documentTitle 문서 제목
     * @param documentId 문서 ID
     * @param rejectReason 반려 사유
     */
    public void createApprovalRejectedNotification(Long requesterId, String approverName, String documentTitle, Long documentId, String rejectReason) {
        try {
            String content = approverName + "님이 결재를 반려했습니다: " + documentTitle;
            if (rejectReason != null && !rejectReason.trim().isEmpty()) {
                content += " (사유: " + rejectReason + ")";
            }

            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(requesterId)
                    .notificationType(NotificationIntranet.NotificationType.APPROVAL_REJECTED)
                    .title("결재 반려")
                    .content(content)
                    .linkUrl("/my-documents.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("결재 반려 알림 생성 완료 - 기안자: {}, 문서: {}", requesterId, documentTitle);
        } catch (Exception e) {
            log.error("결재 반려 알림 생성 실패 - 기안자: {}, 문서: {}", requesterId, documentTitle, e);
        }
    }

    /**
     * 휴가 신청 알림 생성
     * @param approverId 결재자 ID
     * @param requesterName 신청자 이름
     * @param leaveType 휴가 유형 (연차/반차 등)
     * @param scheduleId 일정 ID
     */
    public void createLeaveRequestNotification(Long approverId, String requesterName, String leaveType, Long scheduleId) {
        try {
            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(approverId)
                    .notificationType(NotificationIntranet.NotificationType.APPROVAL_REQUEST)
                    .title("새로운 휴가 신청")
                    .content(requesterName + "님이 " + leaveType + " 신청했습니다")
                    .linkUrl("/approval-pending.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("휴가 신청 알림 생성 완료 - 결재자: {}, 신청자: {}, 유형: {}", approverId, requesterName, leaveType);
        } catch (Exception e) {
            log.error("휴가 신청 알림 생성 실패 - 결재자: {}, 신청자: {}", approverId, requesterName, e);
        }
    }

    /**
     * 휴가 승인 알림 생성
     * @param requesterId 신청자 ID
     * @param approverName 결재자 이름
     * @param leaveType 휴가 유형
     * @param scheduleId 일정 ID
     */
    public void createLeaveApprovedNotification(Long requesterId, String approverName, String leaveType, Long scheduleId) {
        try {
            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(requesterId)
                    .notificationType(NotificationIntranet.NotificationType.LEAVE_APPROVED)
                    .title("휴가 승인")
                    .content(approverName + "님이 " + leaveType + " 승인했습니다")
                    .linkUrl("/schedule.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("휴가 승인 알림 생성 완료 - 신청자: {}, 유형: {}", requesterId, leaveType);
        } catch (Exception e) {
            log.error("휴가 승인 알림 생성 실패 - 신청자: {}, 유형: {}", requesterId, leaveType, e);
        }
    }

    /**
     * 휴가 반려 알림 생성
     * @param requesterId 신청자 ID
     * @param approverName 결재자 이름
     * @param leaveType 휴가 유형
     * @param scheduleId 일정 ID
     * @param rejectReason 반려 사유
     */
    public void createLeaveRejectedNotification(Long requesterId, String approverName, String leaveType, Long scheduleId, String rejectReason) {
        try {
            String content = approverName + "님이 " + leaveType + " 반려했습니다";
            if (rejectReason != null && !rejectReason.trim().isEmpty()) {
                content += " (사유: " + rejectReason + ")";
            }

            NotificationIntranet notification = NotificationIntranet.builder()
                    .memberId(requesterId)
                    .notificationType(NotificationIntranet.NotificationType.LEAVE_REJECTED)
                    .title("휴가 반려")
                    .content(content)
                    .linkUrl("/schedule.html")
                    .isRead(false)
                    .build();

            notificationMapper.insert(notification);
            log.info("휴가 반려 알림 생성 완료 - 신청자: {}, 유형: {}", requesterId, leaveType);
        } catch (Exception e) {
            log.error("휴가 반려 알림 생성 실패 - 신청자: {}, 유형: {}", requesterId, leaveType, e);
        }
    }
}
