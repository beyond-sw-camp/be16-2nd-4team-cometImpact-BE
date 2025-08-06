package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Column(name = "order_number")
    @Builder.Default
    private Integer orderNumber = 0; // Redis에서 증가시킨 값

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

    // 주문 상태의 따라 시간 값 추출 메서드
    public LocalTime getRelevantTime() {
        return switch (status) {
            case ACCEPT   -> LocalTime.from(getAcceptedAt());
            case COMPLETE -> LocalTime.from(getCompletedAt());
            case CANCEL   -> LocalTime.from(getCancelledAt());
        };
    }

}

