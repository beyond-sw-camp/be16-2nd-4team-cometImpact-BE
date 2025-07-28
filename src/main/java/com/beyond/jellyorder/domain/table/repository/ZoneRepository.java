package com.beyond.jellyorder.domain.table.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.table.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    boolean existsByStoreAndName(Store store, String name);

    Optional<Zone> findByStore(Store store);
}
