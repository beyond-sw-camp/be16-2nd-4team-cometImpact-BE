package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unit_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UnitOrder extends BaseIdEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "total_order_id", nullable = false)
    private TotalOrder totalOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "unitOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderMenu> orderMenus = new ArrayList<>();

    // 최초 주문 생성 시 자동 주입
    @PrePersist
    protected void onCreate() {
        this.acceptedAt = LocalDateTime.now();
    }
}

