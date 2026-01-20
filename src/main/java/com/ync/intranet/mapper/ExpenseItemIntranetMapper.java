package com.ync.intranet.mapper;

import com.ync.intranet.domain.ExpenseItemIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 경비 항목 Mapper (인트라넷)
 */
@Mapper
public interface ExpenseItemIntranetMapper {

    /**
     * ID로 경비 항목 조회
     */
    ExpenseItemIntranet findById(@Param("id") Long id);

    /**
     * 경비보고서 ID로 항목 조회
     */
    List<ExpenseItemIntranet> findByExpenseReportId(@Param("expenseReportId") Long expenseReportId);

    /**
     * 카테고리별 항목 조회
     */
    List<ExpenseItemIntranet> findByCategory(@Param("category") String category);

    /**
     * 전체 경비 항목 조회
     */
    List<ExpenseItemIntranet> findAll();

    /**
     * 경비 항목 등록
     */
    void insert(ExpenseItemIntranet expenseItem);

    /**
     * 경비 항목 일괄 등록
     */
    void insertBatch(@Param("items") List<ExpenseItemIntranet> items);

    /**
     * 경비 항목 수정
     */
    void update(ExpenseItemIntranet expenseItem);

    /**
     * 경비 항목 삭제
     */
    void deleteById(@Param("id") Long id);

    /**
     * 경비보고서의 모든 항목 삭제
     */
    void deleteByExpenseReportId(@Param("expenseReportId") Long expenseReportId);

    /**
     * 멤버 ID와 연도로 복지비 항목 조회
     */
    List<ExpenseItemIntranet> findWelfareExpensesByMemberIdAndYear(@Param("memberId") Long memberId, @Param("year") Integer year);

    /**
     * 멤버 ID로 경비 항목 조회
     */
    List<ExpenseItemIntranet> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 멤버 ID와 날짜 범위로 경비 항목 조회
     */
    List<ExpenseItemIntranet> findByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 날짜 범위로 경비 항목 조회
     */
    List<ExpenseItemIntranet> findByDateRange(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * EXPENSE_READ_ID로 경비 항목 조회
     * 조인조건: EXPENSE_ITEM_READ_STATUS.ID = EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID
     */
    List<ExpenseItemIntranet> findByExpenseReadId(@Param("expenseReadId") Long expenseReadId);

    /**
     * EXPENSE_READ_ID 업데이트
     */
    void updateExpenseReadId(@Param("id") Long id, @Param("expenseReadId") Long expenseReadId);

    /**
     * 여러 항목의 EXPENSE_READ_ID 일괄 업데이트
     */
    void updateExpenseReadIdBatch(@Param("ids") List<Long> ids, @Param("expenseReadId") Long expenseReadId);
}
