package com.beyond.jellyorder.domain.category.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 카테고리(Category) 도메인 엔티티.
 * 매장(Store) 내에서 메뉴를 분류하는 단위이며,
 * 이름과 설명을 포함한다.
 *
 * 상위 BaseIdAndTimeEntity를 상속받아 ID, 생성일, 수정일을 공통 관리한다.
 */
@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_category_storeId_name",
                columnNames = {"store_id", "name"}
        )
})
public class Category extends BaseIdAndTimeEntity {

    /**
     * 카테고리를 소속시킬 매장의 고유 식별자.
     * 현재는 테스트 및 개발 편의상 String 타입으로 지정되어 있으며,
     * 추후 Store 타입으로 변경 예정이다.
     */
    @Column(name = "store_id", nullable = false)
    private String storeId;

    /**
     * 카테고리 이름.
     * 예: 디저트, 음료, 샐러드 등
     */
    @Column(length = 20, nullable = false)
    private String name;

    /**
     * 카테고리에 대한 간략한 설명 또는 소개 문구.
     */
    @Column(length = 255, nullable = false)
    private String description;

    // 추후 명시적 Setter 또는 변경 메서드 구현 예정 (예: updateName, updateDescription 등)
}