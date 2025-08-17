package com.beyond.jellyorder.domain.order.entity;


import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_menu_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderMenuOption extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_menu_id", nullable = false)
    private OrderMenu orderMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_option_id", nullable = false)
    private SubOption subOption;

    @Column(name = "option_name", length = 20)
    private String optionName;

    @Column(name = "option_price")
    private Integer optionPrice;

    // 건의 필요
    @Column(name = "option_quantity")
    private Integer optionQuantity;

    public void addOrderMenu(OrderMenu orderMenu) {
        this.orderMenu = orderMenu;
    }
}
