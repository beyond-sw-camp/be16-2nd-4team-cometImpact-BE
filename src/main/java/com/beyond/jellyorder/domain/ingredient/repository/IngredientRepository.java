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
    /**
     * 주어진 storeId와 재료명(name)에 해당하는 재료가 이미 존재하는지 여부를 확인한다.
     *
     * @param storeId 매장 ID (현재는 String이지만, 추후 UUID로 변경 예정)
     * @param name    재료명
     * @return 존재 여부
     */
    boolean existsByStoreIdAndName(String storeId, String name);  // TODO: UUID로 교체
    Optional<Ingredient> findByStoreIdAndName(String storeId, String name);
    List<Ingredient> findAllByStoreId(String storeId);

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
}
