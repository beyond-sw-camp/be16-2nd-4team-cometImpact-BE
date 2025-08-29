package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientDto;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuAdminResDto {
    private UUID id;
    private String category;
    private String name;
    private boolean onSale;
    private Integer price;
    private String imageUrl;
    private String description;     // 유저와 동일
    private String origin;          // 유저와 동일
    private List<MainOptionDto> mainOptions; // ✅ MainOptionDto.selectionType 포함
    private MenuStatus status;

    // 관리자 전용
    private Integer salesLimit;
    private Integer salesToday;
    private List<IngredientDto> ingredients;

    public static MenuAdminResDto fromEntity(Menu menu) {
        List<IngredientDto> ingredientDtos =
                (menu.getMenuIngredients() == null) ? List.of()
                        : menu.getMenuIngredients().stream()
                        .map(MenuIngredient::getIngredient)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                Ingredient::getId,
                                IngredientDto::fromEntity,
                                (a, b) -> a
                        ))
                        .values().stream().toList();

        return MenuAdminResDto.builder()
                .id(menu.getId())
                .category(menu.getCategory() != null ? menu.getCategory().getName() : null)
                .name(menu.getName())
                .onSale(menu.getStockStatus() == MenuStatus.ON_SALE)
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .description(menu.getDescription())
                .origin(menu.getOrigin())
                .mainOptions(
                        menu.getMainOptions() == null ? List.of()
                                : menu.getMainOptions().stream()
                                .map(MainOptionDto::fromEntity) // ✅ selectionType 포함
                                .toList()
                )
                .ingredients(ingredientDtos)
                .salesLimit(menu.getSalesLimit())
                .salesToday(menu.getSalesToday())
                .status(menu.getStockStatus())
                .build();
    }
}
