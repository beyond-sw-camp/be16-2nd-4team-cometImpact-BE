package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.ingredient.dto.IngredientDto;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuListResDto {
    private UUID id;
    private String name;
    private Integer price;
    private String imageUrl;
    private List<MainOptionDto> mainOptions;
    private List<IngredientDto> ingredients;
}
