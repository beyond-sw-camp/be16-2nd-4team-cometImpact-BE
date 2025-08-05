package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.dto.ZoneCreateReqDTO;
import com.beyond.jellyorder.domain.storetable.dto.ZoneListResDTO;
import com.beyond.jellyorder.domain.storetable.dto.ZoneResDTO;
import com.beyond.jellyorder.domain.storetable.dto.ZoneUpdateReqDTO;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import com.beyond.jellyorder.domain.storetable.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final StoreRepository storeRepository;

    // 구역 생성
    public ZoneResDTO createZone(ZoneCreateReqDTO dto) {
        String storeLoginId = getStoreLoginIdByAuth();

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        if (zoneRepository.existsByStoreAndName(store, dto.getZoneName())) {
            throw new DuplicateResourceException("해당 매장에 동일한 구역 이름이 존재합니다.");
        }

        Zone zone = dto.toEntity(store);
        zoneRepository.save(zone);
        return ZoneResDTO.from(zone);
    }


    @Transactional(readOnly = true)
    public List<ZoneListResDTO> getZoneList() {
        String storeLoginId = getStoreLoginIdByAuth();

        List<Zone> zoneList = zoneRepository.findAllByStoreLoginId(storeLoginId);
        return zoneList.stream().map(ZoneListResDTO::from).collect(Collectors.toList());
    }

    // 구역 수정
    public ZoneResDTO updateZone(ZoneUpdateReqDTO dto, UUID zoneId) {
        String storeLoginId = getStoreLoginIdByAuth();
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new EntityNotFoundException("해당 구역이 존재하지 않습니다."));

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        if (!zone.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("해당 매장의 구역이 아닙니다.");
        }

        if (zoneRepository.existsByStoreAndName(store, dto.getZoneName())) {
            throw new DuplicateResourceException("해당 매장에 동일한 구역 이름이 존재합니다.");
        }

        zone.updateName(dto.getZoneName());
        return ZoneResDTO.from(zone);
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
