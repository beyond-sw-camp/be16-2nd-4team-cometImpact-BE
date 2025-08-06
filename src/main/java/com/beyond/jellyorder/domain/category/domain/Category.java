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
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_category_storeId_name",
                columnNames = {"store_id", "name"}
        )
})
public class Category extends BaseIdAndTimeEntity {
    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String description;
}