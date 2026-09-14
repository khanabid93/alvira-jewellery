package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbandonedCartRepository extends JpaRepository<AbandonedCart, Long> {
    AbandonedCart findByPhoneNumber(String phoneNumber);
    List<AbandonedCart> findByRecoveredFalseAndReminderSentFalse();
}