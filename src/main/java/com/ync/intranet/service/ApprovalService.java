package com.ync.intranet.service;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.domain.DocumentIntranet;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.domain.ScheduleIntranet;
import com.ync.intranet.mapper.ApprovalLineIntranetMapper;
import com.ync.intranet.mapper.DocumentIntranetMapper;
import com.ync.intranet.mapper.MemberIntranetMapper;
import com.ync.intranet.mapper.ScheduleIntranetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결재 서비스 (인트라넷)
 */
@Service("approvalServiceIntranet")
@Transactional(readOnly = true)
public class ApprovalService {

    private final ApprovalLineIntranetMapper approvalLineMapper;
    private final DocumentIntranetMapper documentMapper;
    private final ScheduleIntranetMapper scheduleMapper;
    private final MemberIntranetMapper memberMapper;
    private final NotificationService notificationService;

    public ApprovalService(ApprovalLineIntranetMapper approvalLineMapper,
                          DocumentIntranetMapper documentMapper,
                          ScheduleIntranetMapper scheduleMapper,
                          MemberIntranetMapper memberMapper,
                          NotificationService notificationService) {
        this.approvalLineMapper = approvalLineMapper;
        this.documentMapper = documentMapper;
        this.scheduleMapper = scheduleMapper;
        this.memberMapper = memberMapper;
        this.notificationService = notificationService;
    }

    /**
     * 결재자의 대기중인 결재 목록 조회
     */
    public List<ApprovalLineIntranet> getPendingApprovals(Long approverId) {
        return approvalLineMapper.findPendingByApproverId(approverId);
    }

    /**
     * 결재자의 모든 결재 목록 조회
     */
    public List<ApprovalLineIntranet> getApprovalsByApproverId(Long approverId) {
        return approvalLineMapper.findByApproverId(approverId);
    }

    /**
     * 문서의 결재선 조회
     */
    public List<ApprovalLineIntranet> getDocumentApprovals(Long documentId) {
        return approvalLineMapper.findByDocumentId(documentId);
    }

    /**
     * 결재 ID로 조회
     */
    public ApprovalLineIntranet getApprovalById(Long approvalLineId) {
        return approvalLineMapper.findById(approvalLineId);
    }

    /**
     * 결재 승인
     */
    @Transactional
    public void approve(Long approvalLineId, Long approverId, String comment) {
        // 1. 결재선 조회
        ApprovalLineIntranet approvalLine = approvalLineMapper.findById(approvalLineId);
        if (approvalLine == null) {
            throw new RuntimeException("존재하지 않는 결재입니다.");
        }

        // 2. 권한 확인 (본인만 결재 가능)
        if (!approvalLine.getApproverId().equals(approverId)) {
            throw new RuntimeException("결재 권한이 없습니다.");
        }

        // 3. 이미 처리된 결재인지 확인
        if (approvalLine.getDecision() != ApprovalLineIntranet.ApprovalDecision.PENDING) {
            throw new RuntimeException("이미 처리된 결재입니다.");
        }

        // 4. 결재 승인 처리
        approvalLine.setDecision(ApprovalLineIntranet.ApprovalDecision.APPROVED);
        approvalLine.setApprovalComment(comment);
        approvalLine.setDecidedAt(LocalDateTime.now());
        approvalLineMapper.update(approvalLine);

        // 5. 모든 결재가 완료되었는지 확인
        List<ApprovalLineIntranet> allApprovals = approvalLineMapper.findByDocumentId(
                approvalLine.getDocumentId());

        boolean allApproved = allApprovals.stream()
                .allMatch(a -> a.getDecision() == ApprovalLineIntranet.ApprovalDecision.APPROVED);

        // 6. 모든 결재가 완료되면 문서 상태를 APPROVED로 변경
        if (allApproved) {
            documentMapper.approve(approvalLine.getDocumentId());

            // 7. 연결된 일정이 있으면 일정 상태 업데이트
            // 취소 문서인 경우 CANCELLED, 일반 문서인 경우 APPROVED
            DocumentIntranet document = documentMapper.findById(approvalLine.getDocumentId());
            if (document != null && document.getTitle() != null && document.getTitle().startsWith("[취소]")) {
                // 취소 승인: metadata에서 원본 일정 ID를 찾아서 CANCELLED로 변경
                syncCancellationStatus(document);
            } else {
                // 일반 승인: 일정 상태를 APPROVED로 변경
                syncScheduleStatus(approvalLine.getDocumentId(), "APPROVED");
            }

            // 8. 기안자에게 결재 승인 알림 전송
            if (document != null) {
                MemberIntranet approver = memberMapper.findById(approverId);
                String approverName = (approver != null) ? approver.getName() : "관리자";

                notificationService.createApprovalApprovedNotification(
                    document.getAuthorId(),
                    approverName,
                    document.getTitle(),
                    document.getId()
                );
            }
        }
    }

    /**
     * 완료된 결재 목록 조회 (제목/기간 검색 가능)
     */
    public List<ApprovalLineIntranet> getCompletedApprovals(Long approverId, String title, String startDate, String endDate) {
        return approvalLineMapper.findCompletedByApproverId(approverId, title, startDate, endDate);
    }

    /**
     * 결재 반려
     */
    @Transactional
    public void reject(Long approvalLineId, Long approverId, String comment) {
        // 1. 결재선 조회
        ApprovalLineIntranet approvalLine = approvalLineMapper.findById(approvalLineId);
        if (approvalLine == null) {
            throw new RuntimeException("존재하지 않는 결재입니다.");
        }

        // 2. 권한 확인
        if (!approvalLine.getApproverId().equals(approverId)) {
            throw new RuntimeException("결재 권한이 없습니다.");
        }

        // 3. 이미 처리된 결재인지 확인
        if (approvalLine.getDecision() != ApprovalLineIntranet.ApprovalDecision.PENDING) {
            throw new RuntimeException("이미 처리된 결재입니다.");
        }

        // 4. 반려 사유 확인
        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("반려 사유를 입력해야 합니다.");
        }

        // 5. 결재 반려 처리
        approvalLine.setDecision(ApprovalLineIntranet.ApprovalDecision.REJECTED);
        approvalLine.setApprovalComment(comment);
        approvalLine.setDecidedAt(LocalDateTime.now());
        approvalLineMapper.update(approvalLine);

        // 6. 문서 상태를 REJECTED로 변경 (한 명이라도 반려하면 전체 반려)
        documentMapper.reject(approvalLine.getDocumentId());

        // 7. 연결된 일정이 있으면 일정 상태 업데이트
        // 취소 문서인 경우 원본 일정을 APPROVED로 복원, 일반 문서인 경우 REJECTED
        DocumentIntranet document = documentMapper.findById(approvalLine.getDocumentId());
        if (document != null && document.getTitle() != null && document.getTitle().startsWith("[취소]")) {
            // 취소 반려: metadata에서 원본 일정 ID를 찾아서 APPROVED로 복원
            restoreCancellationRejection(document);
        } else {
            // 일반 반려: 일정 상태를 REJECTED로 변경
            syncScheduleStatus(approvalLine.getDocumentId(), "REJECTED");
        }

        // 8. 기안자에게 결재 반려 알림 전송
        if (document != null) {
            MemberIntranet approver = memberMapper.findById(approverId);
            String approverName = (approver != null) ? approver.getName() : "관리자";

            notificationService.createApprovalRejectedNotification(
                document.getAuthorId(),
                approverName,
                document.getTitle(),
                document.getId(),
                comment
            );
        }
    }

    /**
     * 결재 취소 (작성자가 상신 취소)
     */
    @Transactional
    public void cancelApproval(Long documentId, Long authorId) {
        // 1. 문서 조회
        DocumentIntranet document = documentMapper.findById(documentId);
        if (document == null) {
            throw new RuntimeException("존재하지 않는 문서입니다.");
        }

        // 2. 작성자 확인
        if (!document.getAuthorId().equals(authorId)) {
            throw new RuntimeException("작성자만 결재를 취소할 수 있습니다.");
        }

        // 3. PENDING 상태에서만 취소 가능
        if (document.getStatus() != DocumentIntranet.DocumentStatus.PENDING) {
            throw new RuntimeException("결재 대기 중인 문서만 취소할 수 있습니다.");
        }

        // 4. 결재선에서 이미 승인된 건이 있는지 확인
        List<ApprovalLineIntranet> approvals = approvalLineMapper.findByDocumentId(documentId);
        boolean hasApproved = approvals.stream()
                .anyMatch(a -> a.getDecision() == ApprovalLineIntranet.ApprovalDecision.APPROVED);

        if (hasApproved) {
            throw new RuntimeException("이미 승인된 결재가 있어 취소할 수 없습니다.");
        }

        // 5. 결재선 삭제
        approvalLineMapper.deleteByDocumentId(documentId);

        // 6. 문서 상태를 DRAFT로 변경
        documentMapper.updateStatus(documentId, "DRAFT");

        // 7. 연결된 일정이 있으면 일정 상태도 DRAFT로 업데이트
        syncScheduleStatus(documentId, "DRAFT");
    }

    /**
     * 문서와 연결된 일정의 상태를 동기화
     * 승인/반려/취소 시 일정 상태도 함께 업데이트
     */
    private void syncScheduleStatus(Long documentId, String status) {
        try {
            // 문서 ID로 연결된 일정 조회
            List<ScheduleIntranet> schedules = scheduleMapper.findAll();
            for (ScheduleIntranet schedule : schedules) {
                if (schedule.getDocumentId() != null && schedule.getDocumentId().equals(documentId)) {
                    // 연차/반차 일정인 경우에만 상태 동기화
                    if ("VACATION".equals(schedule.getScheduleType()) || "HALF_DAY".equals(schedule.getScheduleType())) {
                        schedule.setStatus(status);
                        scheduleMapper.update(schedule);
                    }
                }
            }
        } catch (Exception e) {
            // 일정 업데이트 실패는 로그만 남기고 결재 처리는 계속 진행
            System.err.println("일정 상태 동기화 실패: " + e.getMessage());
        }
    }

    /**
     * 취소 문서 승인 시 원본 일정의 상태를 CANCELLED로 변경
     * metadata에서 originalScheduleId를 추출하여 사용
     */
    private void syncCancellationStatus(DocumentIntranet cancelDocument) {
        try {
            // metadata에서 원본 일정 ID 추출
            String metadata = cancelDocument.getMetadata();
            if (metadata != null && metadata.contains("originalScheduleId")) {
                // JSON 파싱 (간단한 방법)
                String scheduleIdStr = metadata.replaceAll("[^0-9]", "");
                if (!scheduleIdStr.isEmpty()) {
                    Long originalScheduleId = Long.parseLong(scheduleIdStr);

                    // 원본 일정 조회 및 상태 업데이트
                    ScheduleIntranet schedule = scheduleMapper.findById(originalScheduleId);
                    if (schedule != null) {
                        schedule.setStatus("CANCELLED");
                        scheduleMapper.update(schedule);
                        System.out.println("취소 승인 완료 - 일정 ID: " + originalScheduleId + " 상태를 CANCELLED로 변경");
                    } else {
                        System.err.println("취소 대상 일정을 찾을 수 없습니다. ID: " + originalScheduleId);
                    }
                }
            } else {
                System.err.println("취소 문서에 originalScheduleId 정보가 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("취소 상태 동기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 취소 문서 반려 시 원본 일정의 상태를 APPROVED로 복원
     * metadata에서 originalScheduleId를 추출하여 사용
     */
    private void restoreCancellationRejection(DocumentIntranet cancelDocument) {
        try {
            // metadata에서 원본 일정 ID 추출
            String metadata = cancelDocument.getMetadata();
            if (metadata != null && metadata.contains("originalScheduleId")) {
                // JSON 파싱 (간단한 방법)
                String scheduleIdStr = metadata.replaceAll("[^0-9]", "");
                if (!scheduleIdStr.isEmpty()) {
                    Long originalScheduleId = Long.parseLong(scheduleIdStr);

                    // 원본 일정 조회 및 상태 복원
                    ScheduleIntranet schedule = scheduleMapper.findById(originalScheduleId);
                    if (schedule != null) {
                        schedule.setStatus("APPROVED");
                        scheduleMapper.update(schedule);
                        System.out.println("취소 반려 완료 - 일정 ID: " + originalScheduleId + " 상태를 APPROVED로 복원");
                    } else {
                        System.err.println("취소 대상 일정을 찾을 수 없습니다. ID: " + originalScheduleId);
                    }
                }
            } else {
                System.err.println("취소 문서에 originalScheduleId 정보가 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("취소 반려 상태 복원 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
