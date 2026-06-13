package com.rice.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SaleOrder {

    private Long id;
    private String productType;
    private String productName;
    private Warehouse warehouse;
    private String buyerName;
    private String buyerPhone;
    private String buyerProvince;
    private String buyerCity;
    private String buyerCounty;
    private String buyerAddressDetail;
    private BigDecimal quantity;
    private String unitName;
    private BigDecimal unitToJin;
    private BigDecimal unitPrice;
    private BigDecimal weightJin;
    private BigDecimal pricePerJin;
    private BigDecimal totalAmount;
    private LocalDate soldAt;
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

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerProvince() {
        return buyerProvince;
    }

    public void setBuyerProvince(String buyerProvince) {
        this.buyerProvince = buyerProvince;
    }

    public String getBuyerCity() {
        return buyerCity;
    }

    public void setBuyerCity(String buyerCity) {
        this.buyerCity = buyerCity;
    }

    public String getBuyerCounty() {
        return buyerCounty;
    }

    public void setBuyerCounty(String buyerCounty) {
        this.buyerCounty = buyerCounty;
    }

    public String getBuyerAddressDetail() {
        return buyerAddressDetail;
    }

    public void setBuyerAddressDetail(String buyerAddressDetail) {
        this.buyerAddressDetail = buyerAddressDetail;
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

    public LocalDate getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDate soldAt) {
        this.soldAt = soldAt;
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
