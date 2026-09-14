package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;
    private Long productId;
    private java.time.LocalDateTime addedAt = java.time.LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public java.time.LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(java.time.LocalDateTime addedAt) { this.addedAt = addedAt; }
}