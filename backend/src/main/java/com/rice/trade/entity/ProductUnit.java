package com.rice.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductUnit {

    private Long id;
    private String name;
    private BigDecimal unitToJin;
    private boolean systemBuiltin;
    private Integer sortOrder;
    private boolean enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitToJin() {
        return unitToJin;
    }

    public void setUnitToJin(BigDecimal unitToJin) {
        this.unitToJin = unitToJin;
    }

    public boolean isSystemBuiltin() {
        return systemBuiltin;
    }

    public void setSystemBuiltin(boolean systemBuiltin) {
        this.systemBuiltin = systemBuiltin;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
