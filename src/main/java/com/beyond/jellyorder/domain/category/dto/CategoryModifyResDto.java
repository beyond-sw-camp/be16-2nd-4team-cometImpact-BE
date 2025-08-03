package com.beyond.jellyorder.domain.category.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryModifyResDto {
    private String name;
    private String description;
}