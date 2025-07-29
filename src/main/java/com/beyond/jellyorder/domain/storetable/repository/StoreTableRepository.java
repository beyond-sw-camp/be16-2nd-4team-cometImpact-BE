package com.beyond.jellyorder.domain.storetable.repository;

import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreTableRepository extends JpaRepository<StoreTable, UUID> {
}
