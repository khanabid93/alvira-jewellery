package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class AbandonedCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phoneNumber; // customer or guest phone — one active record per number

    private String customerName; // optional, if known

    @Column(length = 1000)
    private String cartSummary; // e.g. "Gold Necklace x1, Earrings x2"

    private Double cartTotal;

    private java.time.LocalDateTime lastActivityAt = java.time.LocalDateTime.now();

    private boolean recovered = false; // true once they complete checkout
    private boolean reminderSent = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCartSummary() { return cartSummary; }
    public void setCartSummary(String cartSummary) { this.cartSummary = cartSummary; }

    public Double getCartTotal() { return cartTotal; }
    public void setCartTotal(Double cartTotal) { this.cartTotal = cartTotal; }

    public java.time.LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(java.time.LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public boolean isRecovered() { return recovered; }
    public void setRecovered(boolean recovered) { this.recovered = recovered; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
}