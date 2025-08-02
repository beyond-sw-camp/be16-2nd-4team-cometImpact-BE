package com.beyond.jellyorder.domain.menu.domain;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuIngredient {

    @EmbeddedId
    private MenuIngredientId id;

    // FK 관계 제거: 주석 처리
    // @ManyToOne(fetch = FetchType.LAZY)
    // @MapsId("menuId")
    // @JoinColumn(name = "menu_id", referencedColumnName = "id", insertable = false, updatable = false)
    // private Menu menu;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @MapsId("ingredientId")
    // @JoinColumn(name = "ingredient_id", referencedColumnName = "id", insertable = false, updatable = false)
    // private Ingredient ingredient;
}
