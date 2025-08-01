package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.dto.*;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import com.beyond.jellyorder.domain.storetable.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreTableService {

    private final StoreTableRepository storeTableRepository;
    private final StoreRepository storeRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public List<StoreTableResDTO> createTables(StoreTableCreateReqDTO dto, String storeLoginId) {
        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장을 찾을 수 없습니다."));

        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new EntityNotFoundException("해당 구역을 찾을 수 없습니다."));

        if (!zone.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("해당 매장에 속하지 않은 구역입니다.");
        }

        // 중복 이름 필터링
        List<String> requestNames = dto.getStoreTableNameList().stream()
                .map(StoreTableNameReqDTO::getStoreTableName)
                .toList();
        // ex) requestNames = List.of("T1", "T2", "T3");

        List<String> existingNames = storeTableRepository.findNamesByStoreAndNames(store, requestNames);

        if (!existingNames.isEmpty()) {
            throw new DuplicateResourceException("이미 존재하는 테이블 이름입니다: ", existingNames);
        }

        // 중복 없으면 저장
        List<StoreTable> storeTables = dto.toEntityList(store, zone);
        storeTableRepository.saveAll(storeTables);

        return storeTables.stream().map(StoreTableResDTO::from).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<StoreTableListResDTO> getStoreTableList(String storeLoginId) {
        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장을 찾을 수 없습니다."));

        List<Zone> zoneList = zoneRepository.findAllByStoreLoginId(storeLoginId);
        List<StoreTableListResDTO> storeTableListResDTOList = new ArrayList<>();
        for (Zone zone : zoneList) {
            List<StoreTable> storeTableList = storeTableRepository.findAllByZone(zone);
            List<StoreTableDetail> storeTableDetails = storeTableList.stream().map(StoreTableDetail::from).collect(Collectors.toList());
            storeTableListResDTOList.add(StoreTableListResDTO.from(zone, storeTableDetails));
        }

        return storeTableListResDTOList;
    }




}


