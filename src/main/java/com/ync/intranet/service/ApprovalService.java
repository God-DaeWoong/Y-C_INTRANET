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

        // 4. 결재 반려 처리
        approvalLine.setDecision(ApprovalLineIntranet.ApprovalDecision.REJECTED);
        approvalLine.setApprovalComment(comment != null ? comment : "");
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
            System.out.println("[일정 동기화] documentId=" + documentId + ", status=" + status);

            // 문서 ID로 연결된 일정 조회 (효율적인 쿼리 사용)
            List<ScheduleIntranet> schedules = scheduleMapper.findByDocumentId(documentId);

            if (schedules.isEmpty()) {
                System.out.println("[일정 동기화] 연결된 일정이 없음: documentId=" + documentId);
                return;
            }

            for (ScheduleIntranet schedule : schedules) {
                // 연차/반차 일정인 경우에만 상태 동기화
                if ("VACATION".equals(schedule.getScheduleType()) || "HALF_DAY".equals(schedule.getScheduleType())) {
                    System.out.println("[일정 동기화] scheduleId=" + schedule.getId() +
                                     ", 상태 변경: " + schedule.getStatus() + " -> " + status);
                    schedule.setStatus(status);
                    scheduleMapper.update(schedule);
                }
            }

            System.out.println("[일정 동기화 완료] " + schedules.size() + "건 업데이트");
        } catch (Exception e) {
            // 일정 업데이트 실패는 로그만 남기고 결재 처리는 계속 진행
            System.err.println("[일정 동기화 실패] " + e.getMessage());
            e.printStackTrace();
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

    /**
     * 휴가신청 문서에서 일정 생성
     * 문서 제출 시 호출되어 PENDING 상태의 일정을 생성
     */
    @Transactional
    public void createScheduleFromVacationDocument(DocumentIntranet document) {
        try {
            System.out.println("[일정 생성] 휴가신청 문서에서 일정 생성 시작: documentId=" + document.getId() + ", documentType=" + document.getDocumentType());

            // 휴가신청서가 아니면 종료 (LEAVE, VACATION, VACATION_REQUEST 모두 허용)
            String docType = String.valueOf(document.getDocumentType());
            if (!"LEAVE".equals(docType) && !"VACATION".equals(docType) && !"VACATION_REQUEST".equals(docType)) {
                System.out.println("[일정 생성] 휴가신청서가 아니므로 스킵: " + docType);
                return;
            }

            // 문서 content에서 일정 정보 추출
            // 형식: [일정정보:{"scheduleType":"VACATION", "startDate":"2026-01-20", "endDate":"2026-01-22", "daysUsed":3}]
            String content = document.getContent();
            if (content == null || content.isEmpty()) {
                System.err.println("[일정 생성 실패] 문서 내용이 비어있음");
                return;
            }

            // [일정정보:...] 패턴에서 JSON 추출
            String jsonContent = extractScheduleJson(content);
            if (jsonContent == null) {
                System.err.println("[일정 생성 실패] 일정 정보를 찾을 수 없음");
                return;
            }

            // JSON 파싱
            String scheduleType = extractJsonValue(jsonContent, "scheduleType");
            String startDateStr = extractJsonValue(jsonContent, "startDate");
            String endDateStr = extractJsonValue(jsonContent, "endDate");
            String daysUsedStr = extractJsonValue(jsonContent, "daysUsed");

            if (scheduleType == null || startDateStr == null || endDateStr == null) {
                System.err.println("[일정 생성 실패] 필수 일정 정보가 누락됨");
                return;
            }

            // 일정 객체 생성
            ScheduleIntranet schedule = new ScheduleIntranet();
            schedule.setMemberId(document.getAuthorId());
            schedule.setDocumentId(document.getId());
            schedule.setScheduleType(scheduleType); // VACATION or HALF_DAY
            schedule.setTitle(document.getTitle());
            schedule.setDescription(document.getTitle());
            schedule.setStatus("PENDING"); // 결재 대기 상태

            // 날짜 파싱 (java.util.Date) - 타임존 문제 해결
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"));
                schedule.setStartDate(sdf.parse(startDateStr));
                schedule.setEndDate(sdf.parse(endDateStr));
            } catch (Exception e) {
                System.err.println("[일정 생성 실패] 날짜 파싱 오류: " + e.getMessage());
                return;
            }

            // 사용 일수
            if (daysUsedStr != null) {
                try {
                    schedule.setDaysUsed(Double.parseDouble(daysUsedStr));
                } catch (NumberFormatException e) {
                    schedule.setDaysUsed(1.0);
                }
            }

            // DB에 저장
            scheduleMapper.insert(schedule);
            System.out.println("[일정 생성 완료] scheduleId=" + schedule.getId() +
                             ", documentId=" + document.getId() +
                             ", status=PENDING");

        } catch (Exception e) {
            System.err.println("[일정 생성 실패] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * [일정정보:...] 패턴에서 JSON 추출
     */
    private String extractScheduleJson(String content) {
        try {
            String pattern = "\\[일정정보:(.+?)\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("일정 정보 추출 오류: " + e.getMessage());
        }
        return null;
    }

    /**
     * 간단한 JSON 값 추출 헬퍼 메서드
     */
    private String extractJsonValue(String json, String key) {
        try {
            // 문자열 값 추출: "key":"value"
            String pattern1 = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p1 = java.util.regex.Pattern.compile(pattern1);
            java.util.regex.Matcher m1 = p1.matcher(json);
            if (m1.find()) {
                return m1.group(1);
            }

            // 숫자 값 추출: "key":123 or "key":1.5
            String pattern2 = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile(pattern2);
            java.util.regex.Matcher m2 = p2.matcher(json);
            if (m2.find()) {
                return m2.group(1);
            }
        } catch (Exception e) {
            System.err.println("JSON 파싱 오류: " + e.getMessage());
        }
        return null;
    }
}
