package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password; // BCrypt encoded

    private String role = "STAFF"; // "ADMIN" or "STAFF"

    private boolean canManageProducts = false;
    private boolean canManageOrders = false;
    private boolean canManageCoupons = false;
    private boolean canManageOffers = false;
    private boolean canManageLogistics = false;
    private boolean canManageCustomers = false;
    private boolean canViewReports = false;
    private boolean canViewRevenue = false;

    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isCanManageProducts() { return canManageProducts; }
    public void setCanManageProducts(boolean canManageProducts) { this.canManageProducts = canManageProducts; }

    public boolean isCanManageOrders() { return canManageOrders; }
    public void setCanManageOrders(boolean canManageOrders) { this.canManageOrders = canManageOrders; }

    public boolean isCanManageCoupons() { return canManageCoupons; }
    public void setCanManageCoupons(boolean canManageCoupons) { this.canManageCoupons = canManageCoupons; }

    public boolean isCanManageOffers() { return canManageOffers; }
    public void setCanManageOffers(boolean canManageOffers) { this.canManageOffers = canManageOffers; }

    public boolean isCanManageLogistics() { return canManageLogistics; }
    public void setCanManageLogistics(boolean canManageLogistics) { this.canManageLogistics = canManageLogistics; }

    public boolean isCanManageCustomers() { return canManageCustomers; }
    public void setCanManageCustomers(boolean canManageCustomers) { this.canManageCustomers = canManageCustomers; }

    public boolean isCanViewReports() { return canViewReports; }
    public void setCanViewReports(boolean canViewReports) { this.canViewReports = canViewReports; }

    public boolean isCanViewRevenue() { return canViewRevenue; }
    public void setCanViewRevenue(boolean canViewRevenue) { this.canViewRevenue = canViewRevenue; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper: Admin role always has every permission, regardless of individual flags
    public boolean hasPermission(String permission) {
        if ("ADMIN".equals(role)) return true;
        return switch (permission) {
            case "products" -> canManageProducts;
            case "orders" -> canManageOrders;
            case "coupons" -> canManageCoupons;
            case "offers" -> canManageOffers;
            case "logistics" -> canManageLogistics;
            case "customers" -> canManageCustomers;
            case "reports" -> canViewReports;
            case "revenue" -> canViewRevenue;
            default -> false;
        };
    }
}