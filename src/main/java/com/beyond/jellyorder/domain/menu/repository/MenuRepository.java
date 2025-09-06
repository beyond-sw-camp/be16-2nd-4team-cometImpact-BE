package com.beyond.jellyorder.domain.menu.repository;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {

    List<Menu> findAllByCategory_StoreIdAndDeletedFalseAndCategory_DeletedFalse(UUID storeId);

    Optional<Menu> findByIdAndDeletedFalse(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Menu m
       set m.deleted = true,
           m.deletedAt = CURRENT_TIMESTAMP
     where m.id = :id
    """)
    int softDeleteById(@Param("id") UUID id);

    @Query("""
        select (count(m) > 0)
          from Menu m
         where m.deleted = false
           and m.category.deleted = false
           and m.category.store.id = :storeId
           and m.category.name = :categoryName
    """)
    boolean existsAliveMenuByStoreIdAndCategoryName(
            @Param("storeId") UUID storeId,
            @Param("categoryName") String categoryName
    );
}
