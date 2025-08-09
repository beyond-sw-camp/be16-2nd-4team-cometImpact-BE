package com.beyond.jellyorder.domain.option.subOption.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubOptionDto {

    @NotBlank(message = "서브 옵션 이름은 필수입니다.")
    private String name;

    @NotNull(message = "서브 옵션 가격은 필수입니다.")
    @Min(value = 0, message = "서브 옵션 가격은 0 이상이어야 합니다.")
    private Integer price;
}
