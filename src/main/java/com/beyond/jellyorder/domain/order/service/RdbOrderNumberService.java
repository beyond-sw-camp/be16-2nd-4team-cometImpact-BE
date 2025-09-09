package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.order.entity.StoreOrderSequence;
import com.beyond.jellyorder.domain.order.repository.StoreOrderSequenceRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RdbOrderNumberService {
    private final StoreOrderSequenceRepository sequenceRepository;
    private final StoreRepository storeRepository;

    public int nextOrderNo(UUID storeId) {
        StoreOrderSequence seq = sequenceRepository.findByStoreIdForUpdate(storeId)
                .orElseGet(() -> createSeq(storeId));
        return seq.next(); // 0 → 1
    }
    public void reset(UUID storeId) {
        StoreOrderSequence seq = sequenceRepository.findByStoreIdForUpdate(storeId)
                .orElseGet(() -> createSeq(storeId));
        seq.reset(); // 다음 채번은 1
    }
    private StoreOrderSequence createSeq(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다.: " + storeId));
        return sequenceRepository.save(StoreOrderSequence.builder().store(store).lastValue(0).build());
    }
}
