package com.alvira.jewellerystore;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phoneNumber;
    private String address;
    private String city;
    private String pincode;
    private String state;
    private String paymentMode = "Cash on Delivery";
    private java.time.LocalDateTime deliveredDate;
    private String shiprocketShipmentId;
    private String shiprocketOrderId;
    private String awbCode; // tracking number, called AWB (Air Waybill) by Shiprocket
    private String courierName;
    private String shipmentStatus; // e.g. "Pickup Scheduled", "In Transit", "Out for Delivery", "Delivered"
    private java.time.LocalDate estimatedDeliveryDate;
    private String customerEmail;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private String status; // e.g. "PLACED", "SHIPPED", "DELIVERED"

    @Column(length = 2000)
    private String orderSummary; // simple text description of what was ordered

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PLACED";
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
    public java.time.LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(java.time.LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }
    public String getShiprocketShipmentId() {
        return shiprocketShipmentId;
    }

    public void setShiprocketShipmentId(String shiprocketShipmentId) {
        this.shiprocketShipmentId = shiprocketShipmentId;
    }

    public String getShiprocketOrderId() {
        return shiprocketOrderId;
    }

    public void setShiprocketOrderId(String shiprocketOrderId) {
        this.shiprocketOrderId = shiprocketOrderId;
    }

    public String getAwbCode() {
        return awbCode;
    }

    public void setAwbCode(String awbCode) {
        this.awbCode = awbCode;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public java.time.LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(java.time.LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderSummary() {
        return orderSummary;
    }

    public void setOrderSummary(String orderSummary) {
        this.orderSummary = orderSummary;
    }
}