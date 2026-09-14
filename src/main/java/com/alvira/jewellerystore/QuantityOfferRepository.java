package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuantityOfferRepository extends JpaRepository<QuantityOffer, Long> {
    List<QuantityOffer> findByActiveTrue();
}