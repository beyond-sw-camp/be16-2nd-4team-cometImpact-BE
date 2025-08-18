package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.dto.storeTable.*;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import com.beyond.jellyorder.domain.storetable.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    // 테이블 생성
    @Transactional
    public List<StoreTableResDTO> createTables(StoreTableCreateReqDTO dto) {
        String storeLoginId = getStoreLoginIdByAuth();

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장을 찾을 수 없습니다."));

        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new EntityNotFoundException("해당 구역을 찾을 수 없습니다."));

        System.out.println(zone.getId());
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


    // 구역별 전체 테이블 조회
    @Transactional(readOnly = true)
    public List<StoreTableListResDTO> getStoreTableList() {
        String storeLoginId = getStoreLoginIdByAuth();

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

    // 테이블 수정
    public StoreTableResDTO updateStoreTable(StoreTableUpdateReqDTO dto, UUID storeTableId) {
        String storeLoginId = getStoreLoginIdByAuth();

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장을 찾을 수 없습니다."));

        StoreTable storeTable = storeTableRepository.findById(storeTableId)
                .orElseThrow(() -> new EntityNotFoundException("해당 테이블이 존재하지 않습니다."));

        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new EntityNotFoundException("해당 구역을 찾을 수 없습니다."));

        if (!storeTable.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("해당 매장의 테이블이 아닙니다.");
        }

        if (storeTableRepository.existsByStoreAndNameExcludingId(store, dto.getName(), storeTableId)) {
            throw new DuplicateResourceException("해당 매장에 동일한 테이블 이름이 존재합니다.");
        }

        storeTable.updateStoreTableInfo(zone, dto);
        return StoreTableResDTO.from(storeTable);
    }


    public StoreTable doLogin(StoreTableLoginReqDTO storeTableLoginReqDTO) {
        Store store = storeRepository.findByLoginId(storeTableLoginReqDTO.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("로그인 정보가 일치하지 않습니다."));

        StoreTable storeTable = storeTableRepository.findByStoreAndName(store, storeTableLoginReqDTO.getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테이블입니다."));

        if (!passwordEncoder.matches(storeTableLoginReqDTO.getPassword(), store.getPassword())) {
            throw new IllegalArgumentException("로그인 정보가 일치하지 않습니다.");
        }

        return storeTable;
    }

    /**
     * === 내부 공통 메서드 정의 ===
     * 아래는 공통적으로 사용되는 내부메서드를 정의했습니다.
     */

    // Authentication 객체에서 storeLoginId 추출 메서드
    private String getStoreLoginIdByAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

}


