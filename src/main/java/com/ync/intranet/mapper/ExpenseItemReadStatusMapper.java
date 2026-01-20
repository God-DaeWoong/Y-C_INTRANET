package com.ync.intranet.mapper;

import com.ync.intranet.domain.ExpenseItemReadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpenseItemReadStatusMapper {

    void insert(ExpenseItemReadStatus readStatus);

    void insertBatch(List<ExpenseItemReadStatus> readStatuses);

    List<ExpenseItemReadStatus> findByReaderMemberId(@Param("readerMemberId") Long readerMemberId);

    List<ExpenseItemReadStatus> findUnreadByReaderMemberId(@Param("readerMemberId") Long readerMemberId);

    ExpenseItemReadStatus findByExpenseItemIdAndReaderId(
            @Param("expenseItemId") Long expenseItemId,
            @Param("readerMemberId") Long readerMemberId
    );

    void markAsRead(
            @Param("expenseItemId") Long expenseItemId,
            @Param("readerMemberId") Long readerMemberId
    );

    /**
     * READ_STATUS.ID와 readerMemberId로 읽음 처리
     */
    void markAsReadById(
            @Param("id") Long id,
            @Param("readerMemberId") Long readerMemberId
    );

    void deleteByExpenseItemId(@Param("expenseItemId") Long expenseItemId);

    int countUnreadByReaderMemberId(@Param("readerMemberId") Long readerMemberId);

    /**
     * MAX(ID) + 1 조회 (ID가 없으면 1 반환)
     */
    Long getNextId();

    /**
     * ID를 직접 지정하여 INSERT
     */
    void insertWithId(ExpenseItemReadStatus readStatus);
}
