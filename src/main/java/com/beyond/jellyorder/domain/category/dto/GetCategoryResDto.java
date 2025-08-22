package com.beyond.jellyorder.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetCategoryResDto {
    private UUID id;
    private String name;
    private String description;
}
