package com.beyond.jellyorder.domain.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 같은 객체면 true
        if (!(o instanceof MenuIngredientId that)) return false; // 타입이 다르면 false
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(ingredientId, that.ingredientId); // 두 필드 값이 같으면 true
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, ingredientId);
    }
}
