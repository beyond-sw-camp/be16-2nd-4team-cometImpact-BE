package com.beyond.jellyorder.domain.storetable.repository;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // 활성(삭제되지 않은) 이름 중복 체크
    boolean existsByStoreIdAndNameAndDeletedFalse(UUID storeId, String name);

    // 특정 테이블이 진행중 주문과 연결되어 있는지 체크 메서드
    @Query("""
        select case when count(t) > 0 then true else false end
        from TotalOrder t
        where t.storeTable.id = :storeTableId
          and t.endedAt is null
    """)
    boolean existsOpenOrder(UUID storeTableId);

    // 아카이브 포함 단건 조회 (네이티브로 @Where 우회)
    // nativeQuery를 true로 하면 작성한 쿼리 그대로 실행함. 그래서 삭제 된 데이터도 조회 가능.
    @Query(value = "select * from store_table where id = :id", nativeQuery = true)
    Optional<StoreTable> findAnyById(@Param("id") UUID id);

    // 활성(미삭제) 자식 존재 여부
    boolean existsByZone_IdAndArchivedAtIsNull(UUID zoneId);

    /**
     * 소프트 삭제된 자식의 FK 끊기
     * clearAutomatically : DB 업데이트 후 영속성 캐시 싹 밀어 → DB 최신값 신뢰
     * flushAutomatically : DB 업데이트 전에 영속성에 쌓여 있는 dirty 변경을 DB에 반영해 → SQL 실행 순서 보장
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update store_table
               set zone_id = null
             where zone_id = :zoneId
               and archived_at is not null
            """, nativeQuery = true)
    int detachSoftDeletedFromZone(@Param("zoneId") UUID zoneId);



}
