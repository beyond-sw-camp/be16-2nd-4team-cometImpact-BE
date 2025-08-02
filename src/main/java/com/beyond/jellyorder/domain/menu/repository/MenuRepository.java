package com.beyond.jellyorder.domain.menu.repository;

import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository  extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {
    Optional<Menu> findByCategory_StoreIdAndName(String storeId, String name);
}
