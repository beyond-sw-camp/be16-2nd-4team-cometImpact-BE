package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuUserResDto {
    private UUID id;
    private String category;
    private String name;
    private boolean onSale;
    private Integer price;
    private String imageUrl;
    private String description;
    private String origin;
    private List<MainOptionDto> mainOptions;

    public static MenuUserResDto fromEntity(Menu menu) {
        return MenuUserResDto.builder()
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
                                .map(MainOptionDto::fromEntity)
                                .toList()
                )
                .build();
    }
}
