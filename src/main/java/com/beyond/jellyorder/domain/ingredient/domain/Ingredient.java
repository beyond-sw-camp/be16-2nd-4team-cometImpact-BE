package com.beyond.jellyorder.domain.ingredient.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "ingredient",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ingredient_storeId_name",
                        columnNames = {"store_id", "name"}
                )
        }
)
public class Ingredient extends BaseIdAndTimeEntity {

    /** 매장 FK: Store의 PK를 참조 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    @ToString.Exclude
    private Store store;

    /** 재료명 (매장 내에서 유니크) */
    @Column(length = 20, nullable = false)
    private String name;

    /** 재고 상태 */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false) // ENUM 문자열 길이 여유 있게
    private IngredientStatus status = IngredientStatus.SUFFICIENT;

    @OneToMany(
            mappedBy = "ingredient",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private List<MenuIngredient> menuIngredients = new ArrayList<>();
}
