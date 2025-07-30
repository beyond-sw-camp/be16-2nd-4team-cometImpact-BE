package com.beyond.jellyorder.domain.storetable.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "store_table",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_storeTable_name",
                        columnNames = {"store_id", "name"})
        }
)
public class StoreTable extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TableStatus status = TableStatus.STANDBY;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "seat_count", nullable = false)
    private Integer seatCount = 4;




}
