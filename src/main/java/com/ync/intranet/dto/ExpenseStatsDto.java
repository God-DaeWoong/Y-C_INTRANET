package com.ync.intranet.dto;

import java.math.BigDecimal;
import java.util.List;

public class ExpenseStatsDto {
    private int totalCount;
    private BigDecimal totalAmount;
    private List<CategoryStats> categoryStats;

    public ExpenseStatsDto() {
    }

    public ExpenseStatsDto(int totalCount, BigDecimal totalAmount, List<CategoryStats> categoryStats) {
        this.totalCount = totalCount;
        this.totalAmount = totalAmount;
        this.categoryStats = categoryStats;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<CategoryStats> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(List<CategoryStats> categoryStats) {
        this.categoryStats = categoryStats;
    }

    public static class CategoryStats {
        private String category;
        private int count;
        private BigDecimal amount;
        private Double percentage;
        private String welfareFlag; // 'Y' or 'N'

        public CategoryStats() {
        }

        public CategoryStats(String category, int count, BigDecimal amount, Double percentage, String welfareFlag) {
            this.category = category;
            this.count = count;
            this.amount = amount;
            this.percentage = percentage;
            this.welfareFlag = welfareFlag;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public String getWelfareFlag() {
            return welfareFlag;
        }

        public void setWelfareFlag(String welfareFlag) {
            this.welfareFlag = welfareFlag;
        }
    }
}
