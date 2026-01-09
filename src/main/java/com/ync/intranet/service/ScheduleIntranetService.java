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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 일정/휴가 서비스
 */
@Service
@Transactional(readOnly = true)
public class ScheduleIntranetService {

    private final ScheduleIntranetMapper scheduleMapper;
    private final DocumentIntranetMapper documentMapper;
    private final ApprovalLineIntranetMapper approvalLineMapper;
    private final MemberIntranetMapper memberMapper;

    public ScheduleIntranetService(ScheduleIntranetMapper scheduleMapper,
                                   DocumentIntranetMapper documentMapper,
                                   ApprovalLineIntranetMapper approvalLineMapper,
                                   MemberIntranetMapper memberMapper) {
        this.scheduleMapper = scheduleMapper;
        this.documentMapper = documentMapper;
        this.approvalLineMapper = approvalLineMapper;
        this.memberMapper = memberMapper;
    }

    /**
     * 일정 생성
     */
    @Transactional
    public void createSchedule(ScheduleIntranet schedule) {
        // 기본값 설정
        if (schedule.getDaysUsed() == null) {
            schedule.setDaysUsed(0.0);
        }

        // 회의/출장인 경우 결재 불필요 (시간 기반 상태로 저장)
        if ("MEETING".equals(schedule.getScheduleType()) || "BUSINESS_TRIP".equals(schedule.getScheduleType())) {
            schedule.setStatus(calculateMeetingStatus(schedule));
        }
        // 연차/반차인 경우 문서 생성 및 결재 요청
        else if (("VACATION".equals(schedule.getScheduleType()) || "HALF_DAY".equals(schedule.getScheduleType()))
                && schedule.getApproverId() != null) {

            // 1. 문서 생성
            DocumentIntranet document = new DocumentIntranet();
            document.setDocumentType(DocumentIntranet.DocumentType.LEAVE);
            document.setAuthorId(schedule.getMemberId());
            document.setTitle(schedule.getTitle());
            document.setContent(schedule.getDescription() != null ? schedule.getDescription() : "");
            document.setStatus(DocumentIntranet.DocumentStatus.PENDING);
            document.setSubmittedAt(LocalDateTime.now());

            documentMapper.insert(document);

            // 2. 일정에 문서 ID 연결
            schedule.setDocumentId(document.getId());
            schedule.setStatus("SUBMITTED");  // 결재 대기 상태

            // 3. 결재선 생성
            MemberIntranet approver = memberMapper.findById(schedule.getApproverId());
            if (approver != null) {
                ApprovalLineIntranet approvalLine = new ApprovalLineIntranet();
                approvalLine.setDocumentId(document.getId());
                approvalLine.setStepOrder(1);
                approvalLine.setApproverId(schedule.getApproverId());
                approvalLine.setApproverName(approver.getName());
                approvalLine.setApproverPosition(approver.getPosition() != null ? approver.getPosition() : "");
                approvalLine.setDecision(ApprovalLineIntranet.ApprovalDecision.PENDING);
                approvalLine.setSubmittedAt(LocalDateTime.now());

                approvalLineMapper.insert(approvalLine);
            }
        }
        // 기타 경우 (결재자가 없는 연차/반차 등) DRAFT 상태
        else {
            if (schedule.getStatus() == null) {
                schedule.setStatus("DRAFT");
            }
        }

        // 일정 저장
        scheduleMapper.insert(schedule);
    }

    /**
     * 일정 수정
     * - 연차/반차: DRAFT, REJECTED 상태만 수정 가능
     * - 회의/출장: CANCELLED 제외하고 항상 수정 가능
     */
    @Transactional
    public void updateSchedule(ScheduleIntranet schedule) {
        ScheduleIntranet existing = scheduleMapper.findById(schedule.getId());

        if (existing != null) {
            String status = existing.getStatus();
            String scheduleType = existing.getScheduleType();

            // 연차/반차 검증
            if ("VACATION".equals(scheduleType) || "HALF_DAY".equals(scheduleType)) {
                if ("CANCELLED".equals(status) || "SUBMITTED".equals(status) ||
                    "PENDING".equals(status) || "APPROVED".equals(status)) {
                    throw new IllegalStateException("이 일정은 " + status + " 상태로 수정할 수 없습니다.");
                }
            }

            // 회의/출장 검증 (CANCELLED, COMPLETED 불가)
            if ("MEETING".equals(scheduleType) || "BUSINESS_TRIP".equals(scheduleType)) {
                if ("CANCELLED".equals(status) || "COMPLETED".equals(status)) {
                    throw new IllegalStateException("취소되거나 완료된 일정은 수정할 수 없습니다.");
                }
            }
        }

        scheduleMapper.update(schedule);
    }

    /**
     * 일정 삭제
     */
    @Transactional
    public void deleteSchedule(Long id) {
        scheduleMapper.delete(id);
    }

    /**
     * ID로 일정 조회
     *
     * 연차/반차의 경우 문서 상태와 동기화하여 최신 상태 반환
     * 이를 통해 데이터 불일치 문제를 방지
     *
     * 주의: CANCELLED 상태는 동기화하지 않음 (취소 승인 후 상태 유지)
     */
    @Transactional
    public ScheduleIntranet getScheduleById(Long id) {
        ScheduleIntranet schedule = scheduleMapper.findById(id);

        // CANCELLED 상태인 경우 동기화하지 않고 그대로 반환
        // (취소 승인 후 원본 문서가 APPROVED여도 일정은 CANCELLED 유지)
        if (schedule != null && "CANCELLED".equals(schedule.getStatus())) {
            return schedule;
        }

        // 연차/반차이고 문서가 연결된 경우, 문서 상태와 일정 상태 동기화
        if (schedule != null && schedule.getDocumentId() != null &&
            ("VACATION".equals(schedule.getScheduleType()) || "HALF_DAY".equals(schedule.getScheduleType()))) {

            DocumentIntranet document = documentMapper.findById(schedule.getDocumentId());
            if (document != null) {
                // 문서 상태를 일정 상태로 매핑
                String documentStatus = document.getStatus().name();
                String currentScheduleStatus = schedule.getStatus();

                // PENDING -> SUBMITTED (프론트엔드에서는 SUBMITTED로 사용)
                String newStatus;
                if ("PENDING".equals(documentStatus)) {
                    newStatus = "SUBMITTED";
                } else {
                    newStatus = documentStatus;
                }

                // 상태가 변경된 경우 DB에도 반영 (데이터 일관성 유지)
                if (!newStatus.equals(currentScheduleStatus)) {
                    schedule.setStatus(newStatus);
                    scheduleMapper.update(schedule);
                    System.out.println("일정 상태 자동 동기화: ID=" + id + ", " + currentScheduleStatus + " -> " + newStatus);
                }
            }
        }

        return schedule;
    }

    /**
     * 전체 일정 조회
     */
    public List<ScheduleIntranet> getAllSchedules() {
        return scheduleMapper.findAll();
    }

    /**
     * 특정 사용자의 일정 조회
     */
    public List<ScheduleIntranet> getSchedulesByMemberId(Long memberId) {
        return scheduleMapper.findByMemberId(memberId);
    }

    /**
     * 기간별 일정 조회
     */
    public List<ScheduleIntranet> getSchedulesByDateRange(Date startDate, Date endDate) {
        return scheduleMapper.findByDateRange(startDate, endDate);
    }

    /**
     * 부서별 일정 조회
     */
    public List<ScheduleIntranet> getSchedulesByDepartmentAndDateRange(
            Long departmentId, Date startDate, Date endDate) {
        return scheduleMapper.findByDepartmentAndDateRange(departmentId, startDate, endDate);
    }

    /**
     * 일정 취소 신청 (승인된 연차/반차)
     * 기존 문서를 복사하여 취소 문서를 생성하고 결재 요청
     */
    @Transactional
    public void requestCancellation(Long scheduleId) {
        ScheduleIntranet schedule = scheduleMapper.findById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("일정을 찾을 수 없습니다.");
        }

        // 상태 검증: APPROVED 상태만 취소 신청 가능
        if (!"APPROVED".equals(schedule.getStatus())) {
            throw new RuntimeException("승인된 일정만 취소 신청이 가능합니다. 현재 상태: " + schedule.getStatus());
        }

        // 이미 취소된 일정인지 확인
        if ("CANCELLED".equals(schedule.getStatus())) {
            throw new RuntimeException("이미 취소된 일정입니다.");
        }

        // 취소 진행중인지 확인 (PENDING 상태)
        if ("PENDING".equals(schedule.getStatus())) {
            throw new RuntimeException("이미 취소 신청이 진행 중입니다.");
        }

        // 기존 문서 확인
        if (schedule.getDocumentId() == null) {
            throw new RuntimeException("연결된 문서가 없습니다.");
        }

        DocumentIntranet originalDocument = documentMapper.findById(schedule.getDocumentId());
        if (originalDocument == null) {
            throw new RuntimeException("원본 문서를 찾을 수 없습니다.");
        }

        // 기존 결재선 조회 (동일한 결재자에게 취소 결재 요청)
        List<ApprovalLineIntranet> originalApprovals = approvalLineMapper.findByDocumentId(originalDocument.getId());
        if (originalApprovals.isEmpty()) {
            throw new RuntimeException("결재선 정보가 없습니다.");
        }

        // 1. 취소 문서 생성
        DocumentIntranet cancelDocument = new DocumentIntranet();
        cancelDocument.setDocumentType(DocumentIntranet.DocumentType.LEAVE);
        cancelDocument.setAuthorId(schedule.getMemberId());
        cancelDocument.setTitle("[취소] " + schedule.getTitle());
        cancelDocument.setContent("원본 일정 취소 요청\n\n" + (schedule.getDescription() != null ? schedule.getDescription() : ""));
        cancelDocument.setStatus(DocumentIntranet.DocumentStatus.PENDING);
        cancelDocument.setSubmittedAt(LocalDateTime.now());
        // 원본 일정 ID를 metadata에 저장 (취소 승인 시 사용)
        cancelDocument.setMetadata("{\"originalScheduleId\":" + scheduleId + "}");

        documentMapper.insert(cancelDocument);

        // 2. 취소 결재선 생성 (동일한 결재자)
        for (ApprovalLineIntranet originalApproval : originalApprovals) {
            ApprovalLineIntranet cancelApprovalLine = new ApprovalLineIntranet();
            cancelApprovalLine.setDocumentId(cancelDocument.getId());
            cancelApprovalLine.setStepOrder(originalApproval.getStepOrder());
            cancelApprovalLine.setApproverId(originalApproval.getApproverId());
            cancelApprovalLine.setApproverName(originalApproval.getApproverName());
            cancelApprovalLine.setApproverPosition(originalApproval.getApproverPosition());
            cancelApprovalLine.setDecision(ApprovalLineIntranet.ApprovalDecision.PENDING);
            cancelApprovalLine.setSubmittedAt(LocalDateTime.now());

            approvalLineMapper.insert(cancelApprovalLine);
        }

        // 3. 일정 상태를 PENDING으로 변경하고 취소 문서 ID 저장
        // 참고: 취소 승인 시 일정을 CANCELLED로 변경하고 삭제하는 로직은 ApprovalIntranetService에서 처리
        schedule.setStatus("PENDING");
        // 취소 문서 ID를 별도로 저장할 필요가 있다면 metadata나 별도 필드 활용
        // 여기서는 간단히 상태만 변경
        scheduleMapper.update(schedule);
    }

    /**
     * 취소 신청 철회
     * 취소 신청 중인 일정을 다시 승인 상태로 복원
     */
    @Transactional
    public void withdrawCancellation(Long scheduleId, Long userId) {
        // 1. 일정 조회
        ScheduleIntranet schedule = scheduleMapper.findById(scheduleId);
        if (schedule == null) {
            throw new IllegalStateException("일정을 찾을 수 없습니다.");
        }

        // 2. 권한 확인
        if (!schedule.getMemberId().equals(userId)) {
            throw new IllegalStateException("본인의 일정만 철회할 수 있습니다.");
        }

        // 3. 상태 확인 (PENDING 상태만 철회 가능)
        if (!"PENDING".equals(schedule.getStatus())) {
            throw new IllegalStateException("취소 신청 중인 일정만 철회할 수 있습니다.");
        }

        // 4. 취소 문서 찾기 (metadata에 scheduleId가 포함된 문서)
        List<DocumentIntranet> allDocuments = documentMapper.findAll();
        DocumentIntranet cancelDoc = null;
        for (DocumentIntranet doc : allDocuments) {
            if (doc.getTitle() != null && doc.getTitle().startsWith("[취소]") &&
                doc.getMetadata() != null && doc.getMetadata().contains("\"originalScheduleId\":" + scheduleId)) {
                cancelDoc = doc;
                break;
            }
        }

        if (cancelDoc == null) {
            throw new IllegalStateException("취소 신청 문서를 찾을 수 없습니다.");
        }

        // 5. 취소 문서의 결재가 이미 승인되었는지 확인
        if (cancelDoc.getStatus() == DocumentIntranet.DocumentStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 취소 신청은 철회할 수 없습니다.");
        }

        // 6. 결재선 삭제
        approvalLineMapper.deleteByDocumentId(cancelDoc.getId());

        // 7. 취소 문서 삭제
        documentMapper.delete(cancelDoc.getId());

        // 8. 일정 상태를 APPROVED로 복원
        schedule.setStatus("APPROVED");
        scheduleMapper.update(schedule);
    }

    /**
     * 회의/출장 일정의 시간 기반 상태 계산 (KST 기준)
     * RESERVED: 시작 전
     * IN_PROGRESS: 진행 중
     * COMPLETED: 완료
     */
    private String calculateMeetingStatus(ScheduleIntranet schedule) {
        // 한국 표준시(KST) 기준 현재 시간
        ZonedDateTime nowKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        try {
            // 시작 날짜/시간 계산
            ZonedDateTime startDateTime;
            if (schedule.getStartTime() != null && !schedule.getStartTime().isEmpty()) {
                // 시간 정보가 있는 경우
                LocalDateTime startLocal = schedule.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(parseTime(schedule.getStartTime()));
                startDateTime = startLocal.atZone(ZoneId.of("Asia/Seoul"));
            } else {
                // 시간 정보가 없는 경우, 00:00:00으로 설정
                LocalDateTime startLocal = schedule.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atStartOfDay();
                startDateTime = startLocal.atZone(ZoneId.of("Asia/Seoul"));
            }

            // 종료 날짜/시간 계산
            ZonedDateTime endDateTime;
            if (schedule.getEndTime() != null && !schedule.getEndTime().isEmpty()) {
                // 시간 정보가 있는 경우
                LocalDateTime endLocal = schedule.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(parseTime(schedule.getEndTime()));
                endDateTime = endLocal.atZone(ZoneId.of("Asia/Seoul"));
            } else {
                // 시간 정보가 없는 경우, 23:59:59로 설정
                LocalDateTime endLocal = schedule.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(23, 59, 59);
                endDateTime = endLocal.atZone(ZoneId.of("Asia/Seoul"));
            }

            // 상태 판정
            if (nowKST.isBefore(startDateTime)) {
                return "RESERVED";
            } else if (nowKST.isAfter(endDateTime)) {
                return "COMPLETED";
            } else {
                return "IN_PROGRESS";
            }
        } catch (Exception e) {
            // 파싱 오류 시 기본값으로 RESERVED 반환
            return "RESERVED";
        }
    }

    /**
     * 시간 문자열(HH:MI) 파싱
     */
    private java.time.LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return java.time.LocalTime.of(0, 0);
        }
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return java.time.LocalTime.of(hour, minute);
    }

    /**
     * 회의/출장 일정의 상태를 현재 시간 기준으로 업데이트
     * 배치 작업에서 호출됨
     */
    @Transactional
    public int updateMeetingStatuses() {
        // 회의/출장 일정 중 RESERVED, IN_PROGRESS 상태인 것만 조회
        List<ScheduleIntranet> schedules = scheduleMapper.findAll();
        int updateCount = 0;

        for (ScheduleIntranet schedule : schedules) {
            // 회의/출장 타입이고, 완료되지 않은 일정만 처리
            if (("MEETING".equals(schedule.getScheduleType()) || "BUSINESS_TRIP".equals(schedule.getScheduleType()))
                && !"COMPLETED".equals(schedule.getStatus())) {

                String newStatus = calculateMeetingStatus(schedule);

                // 상태가 변경된 경우에만 업데이트
                if (!newStatus.equals(schedule.getStatus())) {
                    schedule.setStatus(newStatus);
                    scheduleMapper.update(schedule);
                    updateCount++;
                }
            }
        }

        return updateCount;
    }
}
