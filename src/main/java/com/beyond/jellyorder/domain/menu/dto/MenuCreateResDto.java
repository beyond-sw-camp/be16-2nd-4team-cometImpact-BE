package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateResDto {
    private UUID id;
    private String name;
    private Integer price;
    private String imageUrl;
    private List<MainOptionDto> mainOptions;

    public static MenuCreateResDto fromEntity(Menu menu) {
        return MenuCreateResDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .mainOptions(
                        menu.getMainOptions() == null ? List.of()
                                : menu.getMainOptions()
                                .stream()
                                .map(MainOptionDto::fromEntity)
                                .toList()
                )
                .build();
    }
}
