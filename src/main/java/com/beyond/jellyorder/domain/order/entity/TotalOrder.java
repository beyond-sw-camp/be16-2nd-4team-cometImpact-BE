package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "total_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TotalOrder extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_table_id", nullable = false)
    private StoreTable storeTable;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Integer totalPrice = 0;

    @Column(name = "count", nullable = false)
    @Builder.Default
    private Integer count = 0;

    @Column(name = "paymented_at")
    private LocalDateTime paymentedAt;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "totalOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnitOrder> unitOrderList = new ArrayList<>();


    // 최초 주문 생성 시 자동 주입
    @PrePersist
    protected void onCreate() {
        this.orderedAt = LocalDateTime.now();
    }

    // 단위주문 총 금액
    public void addUnitTotal(int price, int count) {
        this.totalPrice += price;
        this.count += count;
    }

    // 총 주문 개수 변경 메서드
    public void updateCount(Integer updateCount) {
        this.count += updateCount;
    }

    // 결제 시간 업데이트 메서드
    public void updatePaymentedAt(LocalDateTime paidAt) {
        this.paymentedAt = paidAt;
    }
}
