package com.ync.schedule.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseItem {

    private Long id;
    private Long memberId;
    private LocalDate usageDate;
    private String usageDateStr; // VARCHAR2(20) 컬럼용 문자열 날짜
    private String description;
    private String account;
    private BigDecimal amount;
    private String vendor;
    private String costCode;
    private String projectCode;
    private String note;
    private String welfareFlag;
    private String yyyy;  // 신청 년도 (화면에서 선택한 년도)
    private String mm;    // 신청 월 (화면에서 선택한 월)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Member member;

    public ExpenseItem() {
    }

    public ExpenseItem(Long id, Long memberId, LocalDate usageDate, String description, String account,
                      BigDecimal amount, String vendor, String costCode, String projectCode, String note,
                      String welfareFlag, LocalDateTime createdAt, LocalDateTime updatedAt, Member member) {
        this.id = id;
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

    public static ExpenseItemBuilder builder() {
        return new ExpenseItemBuilder();
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

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    public String getUsageDateStr() {
        return usageDateStr;
    }

    public void setUsageDateStr(String usageDateStr) {
        this.usageDateStr = usageDateStr;
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public static class ExpenseItemBuilder {
        private Long id;
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
        private Member member;

        ExpenseItemBuilder() {
        }

        public ExpenseItemBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExpenseItemBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public ExpenseItemBuilder usageDate(LocalDate usageDate) {
            this.usageDate = usageDate;
            return this;
        }

        public ExpenseItemBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExpenseItemBuilder account(String account) {
            this.account = account;
            return this;
        }

        public ExpenseItemBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public ExpenseItemBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public ExpenseItemBuilder costCode(String costCode) {
            this.costCode = costCode;
            return this;
        }

        public ExpenseItemBuilder projectCode(String projectCode) {
            this.projectCode = projectCode;
            return this;
        }

        public ExpenseItemBuilder note(String note) {
            this.note = note;
            return this;
        }

        public ExpenseItemBuilder welfareFlag(String welfareFlag) {
            this.welfareFlag = welfareFlag;
            return this;
        }

        public ExpenseItemBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExpenseItemBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExpenseItemBuilder member(Member member) {
            this.member = member;
            return this;
        }

        public ExpenseItem build() {
            return new ExpenseItem(id, memberId, usageDate, description, account, amount, vendor,
                                  costCode, projectCode, note, welfareFlag, createdAt, updatedAt, member);
        }
    }
}
