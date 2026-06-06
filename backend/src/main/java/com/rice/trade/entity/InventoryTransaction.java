package com.rice.trade.entity;

import com.rice.trade.enums.BusinessType;
import com.rice.trade.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {

    private Long id;
    private TransactionType transactionType;
    private BusinessType businessType;
    private Long businessId;
    private String productType;
    private String productName;
    private Warehouse warehouse;
    private BigDecimal weightJin;
    private BigDecimal pricePerJin;
    private LocalDateTime occurredAt;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getWeightJin() {
        return weightJin;
    }

    public void setWeightJin(BigDecimal weightJin) {
        this.weightJin = weightJin;
    }

    public BigDecimal getPricePerJin() {
        return pricePerJin;
    }

    public void setPricePerJin(BigDecimal pricePerJin) {
        this.pricePerJin = pricePerJin;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
