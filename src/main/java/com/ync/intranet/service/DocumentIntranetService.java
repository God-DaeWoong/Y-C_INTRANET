package com.ync.intranet.service;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.domain.DocumentIntranet;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.mapper.ApprovalLineIntranetMapper;
import com.ync.intranet.mapper.DocumentIntranetMapper;
import com.ync.intranet.mapper.MemberIntranetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문서 관리 서비스 (인트라넷)
 */
@Service("documentIntranetService")
@Transactional(readOnly = true)
public class DocumentIntranetService {

    private final DocumentIntranetMapper documentMapper;
    private final ApprovalLineIntranetMapper approvalLineMapper;
    private final MemberIntranetMapper memberMapper;
    private final NotificationService notificationService;

    public DocumentIntranetService(DocumentIntranetMapper documentMapper,
                                   ApprovalLineIntranetMapper approvalLineMapper,
                                   MemberIntranetMapper memberMapper,
                                   NotificationService notificationService) {
        this.documentMapper = documentMapper;
        this.approvalLineMapper = approvalLineMapper;
        this.memberMapper = memberMapper;
        this.notificationService = notificationService;
    }

    /**
     * 문서 조회
     */
    public DocumentIntranet findById(Long id) {
        DocumentIntranet document = documentMapper.findById(id);
        if (document != null) {
            // 결재선 정보 함께 조회
            List<ApprovalLineIntranet> approvalLines = approvalLineMapper.findByDocumentId(id);
            document.setApprovalLines(approvalLines);
        }
        return document;
    }

    /**
     * 작성자별 문서 조회
     */
    public List<DocumentIntranet> findByAuthorId(Long authorId) {
        List<DocumentIntranet> documents = documentMapper.findByAuthorId(authorId);
        // 각 문서의 결재선 정보도 함께 조회
        for (DocumentIntranet document : documents) {
            List<ApprovalLineIntranet> approvalLines = approvalLineMapper.findByDocumentId(document.getId());
            document.setApprovalLines(approvalLines);
        }
        return documents;
    }

    /**
     * 작성자 + 상태별 문서 조회
     */
    public List<DocumentIntranet> findByAuthorIdAndStatus(Long authorId, String status) {
        List<DocumentIntranet> documents = documentMapper.findByAuthorIdAndStatus(authorId, status);
        // 각 문서의 결재선 정보도 함께 조회
        for (DocumentIntranet document : documents) {
            List<ApprovalLineIntranet> approvalLines = approvalLineMapper.findByDocumentId(document.getId());
            document.setApprovalLines(approvalLines);
        }
        return documents;
    }

    /**
     * 문서 유형 + 상태별 조회
     */
    public List<DocumentIntranet> findByDocumentTypeAndStatus(String documentType, String status) {
        return documentMapper.findByDocumentTypeAndStatus(documentType, status);
    }

    /**
     * 문서 생성 (임시저장)
     */
    @Transactional
    public DocumentIntranet createDocument(DocumentIntranet document) {
        // 기본값 설정
        if (document.getStatus() == null) {
            document.setStatus(DocumentIntranet.DocumentStatus.DRAFT);
        }

        documentMapper.insert(document);
        return document;
    }

    /**
     * 문서 수정
     */
    @Transactional
    public DocumentIntranet updateDocument(DocumentIntranet document) {
        DocumentIntranet existing = documentMapper.findById(document.getId());
        if (existing == null) {
            throw new RuntimeException("존재하지 않는 문서입니다.");
        }

        // DRAFT 상태에서만 수정 가능
        if (existing.getStatus() != DocumentIntranet.DocumentStatus.DRAFT) {
            throw new RuntimeException("임시저장 상태의 문서만 수정할 수 있습니다.");
        }

        documentMapper.update(document);
        return document;
    }

    /**
     * 문서 결재 상신
     */
    @Transactional
    public void submitDocument(Long documentId, List<ApprovalLineIntranet> approvalLines) {
        DocumentIntranet document = documentMapper.findById(documentId);
        if (document == null) {
            throw new RuntimeException("존재하지 않는 문서입니다.");
        }

        if (document.getStatus() != DocumentIntranet.DocumentStatus.DRAFT) {
            throw new RuntimeException("임시저장 상태의 문서만 상신할 수 있습니다.");
        }

        if (approvalLines == null || approvalLines.isEmpty()) {
            throw new RuntimeException("결재선을 지정해야 합니다.");
        }

        // 1. 문서 상태를 PENDING으로 변경
        documentMapper.submit(documentId);

        // 2. 기안자 정보 조회
        MemberIntranet requester = memberMapper.findById(document.getAuthorId());
        String requesterName = (requester != null) ? requester.getName() : "사용자";

        // 3. 결재선 생성 (결재자 정보 스냅샷)
        for (int i = 0; i < approvalLines.size(); i++) {
            ApprovalLineIntranet line = approvalLines.get(i);
            line.setDocumentId(documentId);
            line.setStepOrder(i + 1);
            line.setDecision(ApprovalLineIntranet.ApprovalDecision.PENDING);
            line.setSubmittedAt(LocalDateTime.now());

            // 결재자 정보 스냅샷 저장
            MemberIntranet approver = memberMapper.findById(line.getApproverId());
            if (approver != null) {
                line.setApproverName(approver.getName());
                line.setApproverPosition(approver.getPosition());
            }

            approvalLineMapper.insert(line);

            // 4. 각 결재자에게 알림 전송
            notificationService.createApprovalRequestNotification(
                line.getApproverId(),
                requesterName,
                document.getTitle(),
                documentId
            );
        }
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        DocumentIntranet document = documentMapper.findById(documentId);
        if (document == null) {
            throw new RuntimeException("존재하지 않는 문서입니다.");
        }

        // 작성자만 삭제 가능
        if (!document.getAuthorId().equals(userId)) {
            throw new RuntimeException("작성자만 문서를 삭제할 수 있습니다.");
        }

        // DRAFT 상태에서만 삭제 가능
        if (document.getStatus() != DocumentIntranet.DocumentStatus.DRAFT) {
            throw new RuntimeException("임시저장 상태의 문서만 삭제할 수 있습니다.");
        }

        documentMapper.deleteById(documentId);
    }
}
