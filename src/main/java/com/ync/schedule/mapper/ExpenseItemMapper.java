package com.ync.schedule.mapper;

import com.ync.schedule.domain.ExpenseItem;
import com.ync.schedule.dto.ExpenseItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseItemMapper {

    List<ExpenseItemDto> findAll();

    ExpenseItem findById(@Param("id") Long id);

    List<ExpenseItemDto> findByMemberId(@Param("memberId") Long memberId);

    List<ExpenseItemDto> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<ExpenseItemDto> findByMemberIdAndDateRange(
        @Param("memberId") Long memberId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<ExpenseItemDto> findWelfareExpensesByMemberIdAndYear(
        @Param("memberId") Long memberId,
        @Param("year") Integer year
    );

    int insert(ExpenseItem expenseItem);

    /**
     * ID를 직접 지정하여 INSERT (EXPENSE_ITEMS_INTRANET.ID를 그대로 사용)
     */
    int insertWithId(ExpenseItem expenseItem);

    int update(ExpenseItem expenseItem);

    int deleteById(@Param("id") Long id);
}
