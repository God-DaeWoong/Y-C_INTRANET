package com.ync.intranet.service;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.domain.DocumentIntranet;
import com.ync.intranet.mapper.ApprovalLineIntranetMapper;
import com.ync.intranet.mapper.DocumentIntranetMapper;
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

    public ApprovalService(ApprovalLineIntranetMapper approvalLineMapper,
                          DocumentIntranetMapper documentMapper) {
        this.approvalLineMapper = approvalLineMapper;
        this.documentMapper = documentMapper;
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
    }
}
