package com.beyond.jellyorder.domain.sales.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesRepository extends JpaRepository<Sales, UUID> {
    Optional<Sales> findByTotalOrderId(UUID totalOrderId);

}
