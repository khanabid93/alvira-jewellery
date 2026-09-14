package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String discountType; // "PERCENTAGE" or "FLAT"
    private Double discountValue;
    private Double minOrderValue;
    private boolean active = true;
    private java.time.LocalDate expiryDate;
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(Double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public java.time.LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(java.time.LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}