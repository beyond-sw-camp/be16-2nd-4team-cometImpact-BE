package com.beyond.jellyorder.domain.storetable.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoreTableRepository extends JpaRepository<StoreTable, UUID> {

    @Query("SELECT st.name FROM StoreTable st WHERE st.store = :store AND st.name IN :names")
    List<String> findNamesByStoreAndNames(@Param("store") Store store, @Param("names") List<String> names);

    List<StoreTable> findAllByZone(Zone zone);

    @Query("""
    SELECT st FROM StoreTable st
    JOIN FETCH st.zone z
    JOIN FETCH st.store s
    WHERE s.loginId = :storeLoginId
""")
    List<StoreTable> findAllByStoreLoginIdWithZoneAndStore(@Param("storeLoginId") String storeLoginId);


}
