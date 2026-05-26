package com.rice.trade.entity;

import com.rice.trade.enums.ProductType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrder {

    private Long id;
    private ProductType productType;
    private String productName;
    private Warehouse warehouse;
    private String counterpartyName;
    private String counterpartyPhone;
    private String counterpartyAddress;
    private BigDecimal quantity;
    private String unitName;
    private BigDecimal unitToJin;
    private BigDecimal unitPrice;
    private BigDecimal weightJin;
    private BigDecimal pricePerJin;
    private BigDecimal totalAmount;
    private LocalDate purchasedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public String getCounterpartyPhone() {
        return counterpartyPhone;
    }

    public void setCounterpartyPhone(String counterpartyPhone) {
        this.counterpartyPhone = counterpartyPhone;
    }

    public String getCounterpartyAddress() {
        return counterpartyAddress;
    }

    public void setCounterpartyAddress(String counterpartyAddress) {
        this.counterpartyAddress = counterpartyAddress;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public BigDecimal getUnitToJin() {
        return unitToJin;
    }

    public void setUnitToJin(BigDecimal unitToJin) {
        this.unitToJin = unitToJin;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDate purchasedAt) {
        this.purchasedAt = purchasedAt;
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
