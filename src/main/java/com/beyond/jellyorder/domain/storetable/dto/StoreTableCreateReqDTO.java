package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @ToString
@NoArgsConstructor
public class StoreTableCreateReqDTO {
    private UUID zoneId;
    private Integer seatCount;
    private List<StoreTableNameReqDTO> storeTableNameList;

    public List<StoreTable> toEntityList(Store store, Zone zone) {
        return storeTableNameList.stream()
                .map(tableNameDTO -> StoreTable.builder()
                        .store(store)
                        .zone(zone)
                        .name(tableNameDTO.getStoreTableName())
                        .seatCount(seatCount) // 기본값 외에도 DTO에서 받은 값 사용
                        .build())
                .collect(Collectors.toList());
    }

    public void validate() {
        if (storeTableNameList == null || storeTableNameList.isEmpty()) {
            throw new IllegalArgumentException("테이블 이름 목록이 비어 있습니다.");
        }

        Set<String> nameSet = new HashSet<>();
        List<String> duplicatedNames = storeTableNameList.stream()
                .map(StoreTableNameReqDTO::getStoreTableName)
                .filter(name -> !nameSet.add(name)) // add 실패 → 중복
                .distinct()
                .toList();

        if (!duplicatedNames.isEmpty()) {
            throw new IllegalArgumentException("요청에 중복된 테이블 이름이 포함되어 있습니다: " + duplicatedNames);
        }
    }
}
