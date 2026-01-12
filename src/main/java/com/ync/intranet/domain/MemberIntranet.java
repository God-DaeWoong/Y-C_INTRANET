package com.ync.intranet.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사원 정보 (인트라넷)
 */
public class MemberIntranet {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private Long departmentId;
    private String departmentName;  // JOIN용
    private String position;
    private String role;  // USER, APPROVER, ADMIN
    private LocalDate hireDate;
    private BigDecimal annualLeaveGranted;
    private Boolean isActive;
    private String smtpPassword;
    private String naverworksUserId;  // 네이버웍스 고유 ID
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberIntranet() {
    }

    public MemberIntranet(Long id, String email, String password, String name, String phone,
                          Long departmentId, String departmentName, String position, String role,
                          LocalDate hireDate, BigDecimal annualLeaveGranted, Boolean isActive,
                          String smtpPassword, String naverworksUserId, LocalDateTime lastLoginAt,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.position = position;
        this.role = role;
        this.hireDate = hireDate;
        this.annualLeaveGranted = annualLeaveGranted;
        this.isActive = isActive;
        this.smtpPassword = smtpPassword;
        this.naverworksUserId = naverworksUserId;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberIntranetBuilder builder() {
        return new MemberIntranetBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getAnnualLeaveGranted() {
        return annualLeaveGranted;
    }

    public void setAnnualLeaveGranted(BigDecimal annualLeaveGranted) {
        this.annualLeaveGranted = annualLeaveGranted;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getNaverworksUserId() {
        return naverworksUserId;
    }

    public void setNaverworksUserId(String naverworksUserId) {
        this.naverworksUserId = naverworksUserId;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    // Role Enum
    public enum Role {
        USER, APPROVER, ADMIN
    }

    // Builder
    public static class MemberIntranetBuilder {
        private Long id;
        private String email;
        private String password;
        private String name;
        private String phone;
        private Long departmentId;
        private String departmentName;
        private String position;
        private String role;
        private LocalDate hireDate;
        private BigDecimal annualLeaveGranted;
        private Boolean isActive;
        private String smtpPassword;
        private String naverworksUserId;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        MemberIntranetBuilder() {
        }

        public MemberIntranetBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MemberIntranetBuilder email(String email) {
            this.email = email;
            return this;
        }

        public MemberIntranetBuilder password(String password) {
            this.password = password;
            return this;
        }

        public MemberIntranetBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MemberIntranetBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public MemberIntranetBuilder departmentId(Long departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public MemberIntranetBuilder departmentName(String departmentName) {
            this.departmentName = departmentName;
            return this;
        }

        public MemberIntranetBuilder position(String position) {
            this.position = position;
            return this;
        }

        public MemberIntranetBuilder role(String role) {
            this.role = role;
            return this;
        }

        public MemberIntranetBuilder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return this;
        }

        public MemberIntranetBuilder annualLeaveGranted(BigDecimal annualLeaveGranted) {
            this.annualLeaveGranted = annualLeaveGranted;
            return this;
        }

        public MemberIntranetBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public MemberIntranetBuilder smtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
            return this;
        }

        public MemberIntranetBuilder naverworksUserId(String naverworksUserId) {
            this.naverworksUserId = naverworksUserId;
            return this;
        }

        public MemberIntranetBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public MemberIntranetBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MemberIntranetBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public MemberIntranet build() {
            return new MemberIntranet(id, email, password, name, phone, departmentId, departmentName,
                    position, role, hireDate, annualLeaveGranted, isActive, smtpPassword,
                    naverworksUserId, lastLoginAt, createdAt, updatedAt);
        }
    }
}
