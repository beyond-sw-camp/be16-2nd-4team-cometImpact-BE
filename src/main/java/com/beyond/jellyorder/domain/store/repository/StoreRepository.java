package com.beyond.jellyorder.domain.store.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByLoginId(String loginId);
    Optional<Store> findByBusinessNumber(String businessNumber);
    Optional<Store> findByOwnerEmail(String email);
    Optional<Store> findById(UUID id);

    Optional<Store> findByOwnerNameAndBusinessNumber(String ownerName, String businessNumber);
}
