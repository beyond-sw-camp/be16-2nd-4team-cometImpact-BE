package com.beyond.jellyorder.domain.storetable.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoreTableRepository extends JpaRepository<StoreTable, UUID> {

    @Query("SELECT st.name FROM StoreTable st WHERE st.store = :store AND st.name IN :names")
    List<String> findNamesByStoreAndNames(@Param("store") Store store, @Param("names") List<String> names);

}
