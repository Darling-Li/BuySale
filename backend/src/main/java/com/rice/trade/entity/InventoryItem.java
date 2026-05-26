package com.rice.trade.entity;

import com.rice.trade.enums.ProductType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItem {

    private Long id;
    private Warehouse warehouse;
    private ProductType productType;
    private String productName;
    private BigDecimal quantityJin;
    private BigDecimal averageCostPerJin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getQuantityJin() {
        return quantityJin;
    }

    public void setQuantityJin(BigDecimal quantityJin) {
        this.quantityJin = quantityJin;
    }

    public BigDecimal getAverageCostPerJin() {
        return averageCostPerJin;
    }

    public void setAverageCostPerJin(BigDecimal averageCostPerJin) {
        this.averageCostPerJin = averageCostPerJin;
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

