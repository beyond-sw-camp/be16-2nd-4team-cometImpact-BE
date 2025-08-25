package com.beyond.jellyorder.domain.menu.repository;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, MenuIngredientId> {
    @Query("""
        select distinct m.id
        from MenuIngredient mi
        join mi.menu m
        where mi.ingredient = :ingredient
    """)
    List<UUID> findMenuIdsByIngredient(@Param("ingredient") Ingredient ingredient);

    @Query("""
           select count(mi)
           from MenuIngredient mi
           where mi.menu.id = :menuId
             and mi.ingredient.status = com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus.EXHAUSTED
           """)
    long countExhaustedByMenuId(@Param("menuId") UUID menuId);
}
