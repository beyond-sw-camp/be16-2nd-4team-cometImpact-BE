package com.beyond.jellyorder.domain.storetable.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface StoreTableRepository extends JpaRepository<StoreTable, UUID> {

    @Query("SELECT st.name FROM StoreTable st WHERE st.store = :store AND st.name IN :names")
    List<String> findNamesByStoreAndNames(@Param("store") Store store, @Param("names") List<String> names);

    List<StoreTable> findAllByZone(Zone zone);
    List<StoreTable> findAllByZoneId(UUID zoneId);

    @Query("""
    SELECT COUNT(st) > 0
    FROM StoreTable st
    WHERE st.store = :store
    AND st.name = :name
    AND st.id <> :excludedId
""")
    boolean existsByStoreAndNameExcludingId(
            @Param("store") Store store,
            @Param("name") String name,
            @Param("excludedId") UUID excludedId
    );

    Optional<StoreTable> findByStoreAndName(Store store, String name);

}
