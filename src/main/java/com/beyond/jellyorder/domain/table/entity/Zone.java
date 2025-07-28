package com.beyond.jellyorder.domain.table.entity;

import com.beyond.jellyorder.common.BaseTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Getter @AllArgsConstructor @NoArgsConstructor @Builder
@Table(
        name = "zone",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_zone_name", columnNames = {"store_id", "name"})
        }
)
public class Zone extends BaseTimeEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private String name;

}
