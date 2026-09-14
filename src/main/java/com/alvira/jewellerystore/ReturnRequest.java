package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String customerEmail;

    private String reason; // dropdown value, mandatory
    @Column(length = 1000)
    private String comments; // optional extra details

    // Bank details for refund
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String accountHolderName;

    // Mandatory proof uploads
    private String productImagePath;
    private String labelImagePath;
    private String unboxingVideoPath;

    private String status = "REQUESTED"; // REQUESTED, APPROVED, REJECTED, REFUNDED
    private java.time.LocalDateTime requestedAt = java.time.LocalDateTime.now();

    @Column(length = 1000)
    private String adminNotes;

    private String rejectionReason; // e.g. "Video Missing", "Invalid Image", "Need More Images"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getProductImagePath() { return productImagePath; }
    public void setProductImagePath(String productImagePath) { this.productImagePath = productImagePath; }

    public String getLabelImagePath() { return labelImagePath; }
    public void setLabelImagePath(String labelImagePath) { this.labelImagePath = labelImagePath; }

    public String getUnboxingVideoPath() { return unboxingVideoPath; }
    public void setUnboxingVideoPath(String unboxingVideoPath) { this.unboxingVideoPath = unboxingVideoPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(java.time.LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}