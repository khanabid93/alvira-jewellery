package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action; // e.g. "Updated Product", "Blocked Customer"
    private String details; // e.g. "Gold Plated Necklace", "Order #5 status -> SHIPPED"
    private java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public java.time.LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
}