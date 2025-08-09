package com.beyond.jellyorder.domain.option.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionAddResDto {
    private String menuId;
    private int addedMainOptionCount;
}
