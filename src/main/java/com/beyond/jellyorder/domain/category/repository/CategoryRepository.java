package com.beyond.jellyorder.domain.category.repository;

import com.beyond.jellyorder.domain.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /* ===== 조회 ===== */

    boolean existsByStoreIdAndNameAndDeletedFalse(UUID storeId, String name);

    List<Category> findAllByStoreIdAndDeletedFalse(UUID storeId);

    List<Category> findAllByStoreIdAndDeletedFalseOrderByUpdatedAtAsc(UUID storeId);

    Optional<Category> findByIdAndStoreIdAndDeletedFalse(UUID id, UUID storeId);

    Optional<Category> findByStoreIdAndNameAndDeletedFalse(UUID storeId, String name);

    // 🔹 삭제본 여러 개일 수 있으므로 “가장 최근 1건”만 가져오기
    Optional<Category> findTopByStoreIdAndNameAndDeletedTrueOrderByDeletedAtDesc(UUID storeId, String name);

    // (필요 시) 삭제본 전부 보고 싶으면 이거 사용
    List<Category> findAllByStoreIdAndNameAndDeletedTrue(UUID storeId, String name);


    /* ===== 소프트 삭제 / 복구 ===== */

    // 소프트 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Category c
         set c.deleted = true,
             c.deletedAt = CURRENT_TIMESTAMP
       where c.store.id = :storeId
         and c.name = :name
         and c.deleted = false
    """)
    int softDeleteByStoreIdAndName(@Param("storeId") UUID storeId,
                                   @Param("name") String name);

    // 🔹 단건 복구 (가장 최근 삭제본의 id로 복구하도록 서비스에서 호출)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Category c
         set c.deleted = false,
             c.deletedAt = null,
             c.description = :description
       where c.id = :id
         and c.deleted = true
    """)
    int restoreById(@Param("id") UUID id,
                    @Param("description") String description);
}
