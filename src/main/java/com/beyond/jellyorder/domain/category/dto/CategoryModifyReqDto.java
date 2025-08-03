package com.beyond.jellyorder.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryModifyReqDto {

    @NotBlank(message = "storeId는 필수입니다.")
    private String storeId;

    @NotBlank(message = "기존 카테고리명은 필수입니다.")
    private String originalName;

    @NotBlank(message = "새 카테고리명은 필수입니다.")
    private String newName;

    @NotBlank(message = "카테고리 설명은 필수입니다.")
    private String newDescription;
}