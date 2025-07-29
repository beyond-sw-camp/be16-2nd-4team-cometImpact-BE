package com.beyond.jellyorder.domain.store.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByLoginId(String loginId);
}
