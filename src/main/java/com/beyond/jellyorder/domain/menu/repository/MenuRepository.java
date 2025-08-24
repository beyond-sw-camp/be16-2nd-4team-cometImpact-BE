package com.beyond.jellyorder.domain.menu.repository;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MenuRepository  extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {
    List<Menu> findAllByCategory_StoreId(UUID category_store_id);
    boolean existsByCategory_StoreIdAndCategory_Name(UUID category_store_id, String category_name);
}
