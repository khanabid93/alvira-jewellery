package com.alvira.jewellerystore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByUsername("admin") == null) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("alvira2026"));
            admin.setRole("ADMIN");
            admin.setCanManageProducts(true);
            admin.setCanManageOrders(true);
            admin.setCanManageCoupons(true);
            admin.setCanManageOffers(true);
            admin.setCanManageLogistics(true);
            admin.setCanManageCustomers(true);
            admin.setCanViewReports(true);
            admin.setCanViewRevenue(true);
            adminUserRepository.save(admin);
        }
    }
}