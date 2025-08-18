package com.beyond.jellyorder.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.beyond.jellyorder.domain.category.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryCustom{
    boolean existsByStoreIdAndName(UUID store_id, String name);
    Optional<Category> findByStoreIdAndName(UUID store_id, String name);
    List<Category> findAllByStoreId(UUID store_id);
    Optional<Category> findByIdAndStoreId(UUID id, UUID store_id);
    int deleteByStore_IdAndName(UUID storeId, String name);
}