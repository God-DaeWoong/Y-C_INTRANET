package com.ync.schedule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseItemDto {

    private Long id;
    private Long memberId;
    private String memberName;
    private String departmentName;
    private LocalDate usageDate;
    private String description;
    private String account;
    private BigDecimal amount;
    private String vendor;
    private String costCode;
    private String projectCode;
    private String note;
    private String welfareFlag;
    private String yyyy;  // 신청 년도
    private String mm;    // 신청 월
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseItemDto() {
    }

    public ExpenseItemDto(Long id, Long memberId, String memberName, String departmentName,
                         LocalDate usageDate, String description, String account, BigDecimal amount,
                         String vendor, String costCode, String projectCode, String note, String welfareFlag,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.departmentName = departmentName;
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
    }

    public static ExpenseItemDtoBuilder builder() {
        return new ExpenseItemDtoBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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

    public String getYyyy() {
        return yyyy;
    }

    public void setYyyy(String yyyy) {
        this.yyyy = yyyy;
    }

    public String getMm() {
        return mm;
    }

    public void setMm(String mm) {
        this.mm = mm;
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

    public static class ExpenseItemDtoBuilder {
        private Long id;
        private Long memberId;
        private String memberName;
        private String departmentName;
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

        ExpenseItemDtoBuilder() {
        }

        public ExpenseItemDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExpenseItemDtoBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public ExpenseItemDtoBuilder memberName(String memberName) {
            this.memberName = memberName;
            return this;
        }

        public ExpenseItemDtoBuilder departmentName(String departmentName) {
            this.departmentName = departmentName;
            return this;
        }

        public ExpenseItemDtoBuilder usageDate(LocalDate usageDate) {
            this.usageDate = usageDate;
            return this;
        }

        public ExpenseItemDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExpenseItemDtoBuilder account(String account) {
            this.account = account;
            return this;
        }

        public ExpenseItemDtoBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public ExpenseItemDtoBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public ExpenseItemDtoBuilder costCode(String costCode) {
            this.costCode = costCode;
            return this;
        }

        public ExpenseItemDtoBuilder projectCode(String projectCode) {
            this.projectCode = projectCode;
            return this;
        }

        public ExpenseItemDtoBuilder note(String note) {
            this.note = note;
            return this;
        }

        public ExpenseItemDtoBuilder welfareFlag(String welfareFlag) {
            this.welfareFlag = welfareFlag;
            return this;
        }

        public ExpenseItemDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExpenseItemDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExpenseItemDto build() {
            return new ExpenseItemDto(id, memberId, memberName, departmentName, usageDate, description,
                                     account, amount, vendor, costCode, projectCode, note, welfareFlag, createdAt, updatedAt);
        }
    }
}
