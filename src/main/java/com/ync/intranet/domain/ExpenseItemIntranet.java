package com.ync.intranet.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 경비 항목 (인트라넷)
 */
public class ExpenseItemIntranet {

    private Long id;
    private Long expenseReportId;
    private Long memberId;
    private LocalDate usageDate;
    private String description;
    private String account;
    private BigDecimal amount;
    private String vendor;
    private String costCode;
    private String projectCode;
    private String note;
    private String welfareFlag;
    private Long expenseReadId;  // EXPENSE_ITEM_READ_STATUS와 연동되는 ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MemberIntranet member;

    public ExpenseItemIntranet() {
    }

    public ExpenseItemIntranet(Long id, Long expenseReportId, Long memberId, LocalDate usageDate,
                               String description, String account, BigDecimal amount, String vendor,
                               String costCode, String projectCode, String note, String welfareFlag,
                               LocalDateTime createdAt, LocalDateTime updatedAt, MemberIntranet member) {
        this.id = id;
        this.expenseReportId = expenseReportId;
        this.memberId = memberId;
        this.usageDate = usageDate;
        this.description = description;
        this.account = account;
        this.amount = amount;
        this.vendor = vendor;
        this.costCode = costCode;
        this.projectCode = projectCode;
        this.note = note;
        this.welfareFlag = welfareFlag;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.member = member;
    }

    public static ExpenseItemIntranetBuilder builder() {
        return new ExpenseItemIntranetBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpenseReportId() {
        return expenseReportId;
    }

    public void setExpenseReportId(Long expenseReportId) {
        this.expenseReportId = expenseReportId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCostCode() {
        return costCode;
    }

    public void setCostCode(String costCode) {
        this.costCode = costCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getWelfareFlag() {
        return welfareFlag;
    }

    public void setWelfareFlag(String welfareFlag) {
        this.welfareFlag = welfareFlag;
    }

    public Long getExpenseReadId() {
        return expenseReadId;
    }

    public void setExpenseReadId(Long expenseReadId) {
        this.expenseReadId = expenseReadId;
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

    public MemberIntranet getMember() {
        return member;
    }

    public void setMember(MemberIntranet member) {
        this.member = member;
    }

    // Convenience getters for member info
    public String getMemberName() {
        return member != null ? member.getName() : null;
    }

    public String getDepartmentName() {
        return member != null ? member.getDepartmentName() : null;
    }

    // Builder
    public static class ExpenseItemIntranetBuilder {
        private Long id;
        private Long expenseReportId;
        private Long memberId;
        private LocalDate usageDate;
        private String description;
        private String account;
        private BigDecimal amount;
        private String vendor;
        private String costCode;
        private String projectCode;
        private String note;
        private String welfareFlag;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private MemberIntranet member;

        ExpenseItemIntranetBuilder() {
        }

        public ExpenseItemIntranetBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExpenseItemIntranetBuilder expenseReportId(Long expenseReportId) {
            this.expenseReportId = expenseReportId;
            return this;
        }

        public ExpenseItemIntranetBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public ExpenseItemIntranetBuilder usageDate(LocalDate usageDate) {
            this.usageDate = usageDate;
            return this;
        }

        public ExpenseItemIntranetBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExpenseItemIntranetBuilder account(String account) {
            this.account = account;
            return this;
        }

        public ExpenseItemIntranetBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public ExpenseItemIntranetBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public ExpenseItemIntranetBuilder costCode(String costCode) {
            this.costCode = costCode;
            return this;
        }

        public ExpenseItemIntranetBuilder projectCode(String projectCode) {
            this.projectCode = projectCode;
            return this;
        }

        public ExpenseItemIntranetBuilder note(String note) {
            this.note = note;
            return this;
        }

        public ExpenseItemIntranetBuilder welfareFlag(String welfareFlag) {
            this.welfareFlag = welfareFlag;
            return this;
        }

        public ExpenseItemIntranetBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExpenseItemIntranetBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExpenseItemIntranetBuilder member(MemberIntranet member) {
            this.member = member;
            return this;
        }

        public ExpenseItemIntranet build() {
            return new ExpenseItemIntranet(id, expenseReportId, memberId, usageDate,
                    description, account, amount, vendor, costCode, projectCode,
                    note, welfareFlag, createdAt, updatedAt, member);
        }
    }
}
