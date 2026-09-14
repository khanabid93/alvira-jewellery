package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByCustomerEmail(String customerEmail);
    ReturnRequest findByOrderId(Long orderId);
}