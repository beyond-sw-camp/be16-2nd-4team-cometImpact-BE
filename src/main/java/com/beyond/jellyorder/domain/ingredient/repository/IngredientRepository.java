package com.beyond.jellyorder.domain.ingredient.repository;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID>, IngredientRepositoryCustom{
    boolean existsByStoreIdAndName(UUID store_id, String name);  // TODO: UUID로 교체
    Optional<Ingredient> findByStoreIdAndName(UUID store_id, String name);
    List<Ingredient> findAllByStoreId(UUID store_id);

    interface AffectedMenu {
        String getId();   // BIN_TO_UUID 결과를 받음
        String getName();
    }

    @Query(value = """
        SELECT BIN_TO_UUID(m.id) AS id, m.name AS name
        FROM menu m
        JOIN menu_ingredient mi ON mi.menu_id = m.id
        WHERE mi.ingredient_id = :ingredientId
        """, nativeQuery = true)
    List<AffectedMenu> findAffectedMenus(@Param("ingredientId") UUID ingredientId);

    boolean existsByStoreIdAndNameAndIdNot(UUID store_id, String name, UUID id);
    Optional<Ingredient> findByIdAndStoreId(UUID id, UUID store_id);
}
