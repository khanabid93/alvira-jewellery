package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByCustomerEmail(String customerEmail);
    WishlistItem findByCustomerEmailAndProductId(String customerEmail, Long productId);

    @Transactional
    void deleteByCustomerEmailAndProductId(String customerEmail, Long productId);
}