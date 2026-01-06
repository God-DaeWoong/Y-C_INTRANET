package com.ync.intranet.mapper;

import com.ync.intranet.domain.ApprovalLineIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 결재선 Mapper (인트라넷)
 */
@Mapper
public interface ApprovalLineIntranetMapper {

    /**
     * 결재선 조회 (ID)
     */
    ApprovalLineIntranet findById(@Param("id") Long id);

    /**
     * 문서별 결재선 조회 (순서대로)
     */
    List<ApprovalLineIntranet> findByDocumentId(@Param("documentId") Long documentId);

    /**
     * 결재자별 대기중인 결재 조회
     */
    List<ApprovalLineIntranet> findPendingByApproverId(@Param("approverId") Long approverId);

    /**
     * 결재자별 모든 결재 조회
     */
    List<ApprovalLineIntranet> findByApproverId(@Param("approverId") Long approverId);

    /**
     * 결재자별 완료된 결재 조회 (제목, 날짜 검색 가능)
     */
    List<ApprovalLineIntranet> findCompletedByApproverId(@Param("approverId") Long approverId,
                                                          @Param("title") String title,
                                                          @Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);

    /**
     * 문서 + 결재 순서로 조회
     */
    ApprovalLineIntranet findByDocumentIdAndStepOrder(@Param("documentId") Long documentId,
                                                       @Param("stepOrder") Integer stepOrder);

    /**
     * 결재선 등록
     */
    void insert(ApprovalLineIntranet approvalLine);

    /**
     * 결재선 일괄 등록
     */
    void insertBatch(@Param("approvalLines") List<ApprovalLineIntranet> approvalLines);

    /**
     * 결재 처리 (승인/반려)
     */
    void update(ApprovalLineIntranet approvalLine);

    /**
     * 결재선 삭제
     */
    void deleteById(@Param("id") Long id);

    /**
     * 문서의 모든 결재선 삭제
     */
    void deleteByDocumentId(@Param("documentId") Long documentId);
}
