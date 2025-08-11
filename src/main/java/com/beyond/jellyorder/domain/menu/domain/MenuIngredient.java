package com.beyond.jellyorder.domain.menu.domain;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_ingredient")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuIngredient {

    @EmbeddedId
    @Builder.Default
    private MenuIngredientId id = new MenuIngredientId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("menuId")
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
}
