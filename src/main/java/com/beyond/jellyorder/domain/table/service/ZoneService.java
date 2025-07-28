package com.beyond.jellyorder.domain.table.service;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.table.dto.ZoneCreateReqDTO;
import com.beyond.jellyorder.domain.table.dto.ZoneResDTO;
import com.beyond.jellyorder.domain.table.entity.Zone;
import com.beyond.jellyorder.domain.table.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final StoreRepository storeRepository;

    public ZoneResDTO createZone(ZoneCreateReqDTO dto, String storeLoginId) {
        /*
          추후 Authentication 도입 후 수정할 로직.
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          String storeLoginId = authentication.getName();
         */

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        if (zoneRepository.existsByStoreAndName(store, dto.getZoneName())) {
            throw new IllegalArgumentException("해당 매장에 동일한 구역 이름이 존재합니다.");
        }

        Zone zone = dto.toEntity(store);
        zoneRepository.save(zone);
        return ZoneResDTO.from(zone);
    }
}
