package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    AdminUser findByUsername(String username);
}