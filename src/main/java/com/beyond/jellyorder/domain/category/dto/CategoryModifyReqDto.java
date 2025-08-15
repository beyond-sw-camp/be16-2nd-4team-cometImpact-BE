package com.beyond.jellyorder.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryModifyReqDto {
    @NotNull
    private UUID categoryId;

    @NotBlank
    private String newName;

    @NotBlank
    private String newDescription;
}