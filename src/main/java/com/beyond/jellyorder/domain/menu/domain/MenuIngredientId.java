package com.beyond.jellyorder.domain.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuIngredientId implements Serializable {

    @Column(name = "menu_id", columnDefinition = "BINARY(16)")
    private UUID menuId;

    @Column(name = "ingredient_id", columnDefinition = "BINARY(16)")
    private UUID ingredientId;

    // equals, hashCode 반드시 구현해야 함
}
