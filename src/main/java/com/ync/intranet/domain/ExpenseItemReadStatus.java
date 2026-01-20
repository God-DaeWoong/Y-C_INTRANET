package com.ync.intranet.domain;

import java.time.LocalDateTime;

public class ExpenseItemReadStatus {
    private Long id;
    private Long expenseItemId;
    private Long readerMemberId;
    private String readYn;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인용 필드
    private String readerMemberName;
    private String readerDepartmentName;

    public ExpenseItemReadStatus() {
    }

    public ExpenseItemReadStatus(Long id, Long expenseItemId, Long readerMemberId, String readYn,
                                 LocalDateTime readAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                                 String readerMemberName, String readerDepartmentName) {
        this.id = id;
        this.expenseItemId = expenseItemId;
        this.readerMemberId = readerMemberId;
        this.readYn = readYn;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.readerMemberName = readerMemberName;
        this.readerDepartmentName = readerDepartmentName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpenseItemId() {
        return expenseItemId;
    }

    public void setExpenseItemId(Long expenseItemId) {
        this.expenseItemId = expenseItemId;
    }

    public Long getReaderMemberId() {
        return readerMemberId;
    }

    public void setReaderMemberId(Long readerMemberId) {
        this.readerMemberId = readerMemberId;
    }

    public String getReadYn() {
        return readYn;
    }

    public void setReadYn(String readYn) {
        this.readYn = readYn;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReaderMemberName() {
        return readerMemberName;
    }

    public void setReaderMemberName(String readerMemberName) {
        this.readerMemberName = readerMemberName;
    }

    public String getReaderDepartmentName() {
        return readerDepartmentName;
    }

    public void setReaderDepartmentName(String readerDepartmentName) {
        this.readerDepartmentName = readerDepartmentName;
    }
}
