package com.beyond.jellyorder.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 카테고리 생성 API의 응답 DTO.
 * 클라이언트에게 생성된 카테고리의 정보를 반환할 때 사용된다.
 *
 * 필드에 final을 사용함으로써 불변 객체(immutable)로 유지되어
 * 응답 데이터의 안정성과 무결성을 보장한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class CategoryCreateResDto {

    /**
     * 생성된 카테고리의 고유 ID (UUID 형식)
     */
    private final UUID id;

    /**
     * 카테고리 이름 (예: 디저트, 음료 등)
     */
    private final String name;

    /**
     * 카테고리에 대한 설명 또는 소개 문구
     */
    private final String description;
}
