package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private java.time.LocalDateTime subscribedAt = java.time.LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public java.time.LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(java.time.LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }
}