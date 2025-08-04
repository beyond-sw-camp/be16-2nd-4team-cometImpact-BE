package com.beyond.jellyorder.domain.request.service;

import com.beyond.jellyorder.domain.request.dto.RequestRdbDto;
import com.beyond.jellyorder.domain.request.repository.RequestRdbRepository;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestResponseDto;
import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RdbRequestService {

    private final RequestRdbRepository requestRdbRepository;
    private final StoreRepository storeRepository;

    // 요청사항 생성
    public UUID create(RequestRdbDto dto) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Store store = storeRepository.findByOwnerEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));
//
//        Request request = Request.builder()
//                .storeId(store)
//                .name(dto.getName())
//                .build();

        
        // 테스트용
        String email = "test@naver.com";

        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        Request request = Request.builder()
                .store(store)
                .name(dto.getName())
                .build();

        Request saved = requestRdbRepository.save(request);
        return saved.getId();
    }

    // 요청사항 전체 조회 (해당 점주의 매장 기준)
    public List<RequestResponseDto> getMyRequests() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Store store = storeRepository.findByOwnerEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));
//
//        List<Request> requests = requestRdbRepository.findAllByStoreId(store);
//
//        return requests.stream()
//                .map(r -> RequestResponseDto.builder()
//                        .id(r.getId())
//                        .name(r.getName())
//                        .storeId(String.valueOf(store.getId()))
//                        .storeName(store.getStoreName())
//                        .build())
//                .toList();

        
        // 테스트용
        String email = "test@naver.com";
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        List<Request> requests = requestRdbRepository.findAllByStore(store);

        return requests.stream()
                .map(r -> RequestResponseDto.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .build())
                .toList();
    }
}

