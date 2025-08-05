package com.beyond.jellyorder.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.beyond.jellyorder.domain.category.domain.Category;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryCustom{
    boolean existsByStoreIdAndName(String storeId, String name); // 임시 String storeId. **추후 UUID로 변경 필수**
    Optional<Category> findByStoreIdAndName(String storeId, String name);
    List<Category> findAllByStoreId(String storeId);

    @Modifying
    @Query("DELETE FROM Category c WHERE c.storeId = :storeId AND c.name = :name")
    int deleteByStoreIdAndName(@Param("storeId") String storeId, @Param("name") String name);

}