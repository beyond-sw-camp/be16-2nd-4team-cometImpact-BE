package com.beyond.jellyorder.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.beyond.jellyorder.domain.category.domain.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryCustom{
    boolean existsByStoreIdAndName(String storeId, String name); // 임시 String storeId. **추후 UUID로 변경 필수**
}
