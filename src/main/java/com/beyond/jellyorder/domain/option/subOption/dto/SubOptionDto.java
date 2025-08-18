package com.beyond.jellyorder.domain.option.subOption.dto;

import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
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

    public SubOption toEntity() {
        String n = (name == null) ? "" : name.trim();
        if (n.isEmpty()) throw new IllegalArgumentException("서브 옵션 이름은 필수입니다.");
        if (price == null) throw new IllegalArgumentException("서브 옵션 가격은 필수입니다.");
        if (price < 0) throw new IllegalArgumentException("서브 옵션 가격은 0 이상이어야 합니다.");

        return SubOption.builder()
                .name(n)
                .price(price)
                .build();
    }

    public static SubOptionDto fromEntity(SubOption e) {
        return SubOptionDto.builder()
                .name(e.getName())
                .price(e.getPrice())
                .build();
    }
}
