package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "order_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderMenu extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_order_id", nullable = false)
    private UnitOrder unitOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    // 주문 수량 변경 메서드
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}