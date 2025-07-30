package com.beyond.jellyorder.domain.table.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.table.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    boolean existsByStoreAndName(Store store, String name);

    List<Zone> findAllByStoreLoginId(String loginId);
}
