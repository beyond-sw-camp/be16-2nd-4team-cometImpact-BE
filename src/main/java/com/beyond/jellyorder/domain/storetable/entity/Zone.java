package com.beyond.jellyorder.domain.storetable.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @AllArgsConstructor @NoArgsConstructor @Builder
@ToString
@Table(
        name = "zone",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_zone_name", columnNames = {"store_id", "name"})
        }
)
public class Zone extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private String name;

    public void updateName(String name) {
        this.name = name;
    }

}
