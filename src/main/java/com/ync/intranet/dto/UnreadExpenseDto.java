package com.ync.intranet.dto;

import com.ync.intranet.domain.ExpenseItemIntranet;

import java.time.LocalDateTime;
import java.util.List;

public class UnreadExpenseDto {
    private Long expenseItemId;
    private String submitterName;
    private String submitterDepartment;
    private LocalDateTime submittedAt;
    private List<ExpenseItemIntranet> items;
    private int itemCount;

    public UnreadExpenseDto() {
    }

    public UnreadExpenseDto(Long expenseItemId, String submitterName, String submitterDepartment,
                           LocalDateTime submittedAt, List<ExpenseItemIntranet> items, int itemCount) {
        this.expenseItemId = expenseItemId;
        this.submitterName = submitterName;
        this.submitterDepartment = submitterDepartment;
        this.submittedAt = submittedAt;
        this.items = items;
        this.itemCount = itemCount;
    }

    public Long getExpenseItemId() {
        return expenseItemId;
    }

    public void setExpenseItemId(Long expenseItemId) {
        this.expenseItemId = expenseItemId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterDepartment() {
        return submitterDepartment;
    }

    public void setSubmitterDepartment(String submitterDepartment) {
        this.submitterDepartment = submitterDepartment;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<ExpenseItemIntranet> getItems() {
        return items;
    }

    public void setItems(List<ExpenseItemIntranet> items) {
        this.items = items;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
