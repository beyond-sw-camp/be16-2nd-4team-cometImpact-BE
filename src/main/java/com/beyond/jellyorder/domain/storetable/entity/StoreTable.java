package com.beyond.jellyorder.domain.storetable.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.common.auth.Role;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.dto.storeTable.StoreTableUpdateReqDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

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
// 삭제 쿼리 예시) "테이블1__deleted_20250827121530_9x82lm"
@SQLDelete(sql =
        "update store_table " +
                "set deleted = true, " +
                "    archived_at = now(), " +
                "    name = concat(" +
                "      substring(name, 1, 80), " +
                "      '__deleted_', date_format(now(), '%Y%m%d%H%i%S'), '_'," +
                "      lpad(conv(floor(rand()*pow(36,6)),10,36),6,'0')" +
                "    ) " +
                "where id = ?"
)
@Where(clause = "deleted = false")
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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.STORE_TABLE;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false; // 삭제 여부

    private LocalDateTime archivedAt; // 삭제 시간


    //==테이블 수정 메서드==//
    public void updateStoreTableInfo(Zone zone, StoreTableUpdateReqDTO dto) {
        this.zone = zone;
        this.name = dto.getName();
        this.seatCount = dto.getSeatCount();
    }

    // 테이블 상태 변화
    public void changeStatus(TableStatus newStatus) {
        this.status = newStatus;
    }
}
