package com.rice.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrder {

    private Long id;
    private String productType;
    private String productName;
    private Warehouse warehouse;
    private String counterpartyName;
    private String counterpartyPhone;
    private String counterpartyProvince;
    private String counterpartyCity;
    private String counterpartyCounty;
    private String counterpartyAddressDetail;
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

    public String getCounterpartyProvince() {
        return counterpartyProvince;
    }

    public void setCounterpartyProvince(String counterpartyProvince) {
        this.counterpartyProvince = counterpartyProvince;
    }

    public String getCounterpartyCity() {
        return counterpartyCity;
    }

    public void setCounterpartyCity(String counterpartyCity) {
        this.counterpartyCity = counterpartyCity;
    }

    public String getCounterpartyCounty() {
        return counterpartyCounty;
    }

    public void setCounterpartyCounty(String counterpartyCounty) {
        this.counterpartyCounty = counterpartyCounty;
    }

    public String getCounterpartyAddressDetail() {
        return counterpartyAddressDetail;
    }

    public void setCounterpartyAddressDetail(String counterpartyAddressDetail) {
        this.counterpartyAddressDetail = counterpartyAddressDetail;
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
