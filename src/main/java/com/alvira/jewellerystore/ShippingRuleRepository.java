package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {
    List<ShippingRule> findAllByOrderByPriorityAsc();
}