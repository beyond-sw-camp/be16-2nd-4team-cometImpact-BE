package com.beyond.jellyorder.domain.menu.repository;

import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, MenuIngredientId> {
}
