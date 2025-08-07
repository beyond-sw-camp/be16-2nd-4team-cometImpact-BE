package com.beyond.jellyorder.domain.category.dto;

import lombok.*;


@Builder
public record CategoryModifyResDto(String name, String description) {
}