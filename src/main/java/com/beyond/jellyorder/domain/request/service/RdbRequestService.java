package com.beyond.jellyorder.domain.request.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.request.dto.RequestRdbDto;
import com.beyond.jellyorder.domain.request.dto.RequestUpdateReqDto;
import com.beyond.jellyorder.domain.request.dto.RequestUpdateResDto;
import com.beyond.jellyorder.domain.request.repository.RequestRdbRepository;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestResponseDto;
import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RdbRequestService {

    private final RequestRdbRepository requestRdbRepository;
    private final StoreRepository storeRepository;
    private final StoreJwtClaimUtil storeJwtClaimUtil;

    // 요청사항 생성
    public UUID create(RequestRdbDto dto) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
        }

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        Store store = storeRepository.findByLoginId(loginId)
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
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
        }

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        Store store = storeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        List<Request> requests = requestRdbRepository.findAllByStore(store);

        return requests.stream()
                .map(r -> RequestResponseDto.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .storeId(String.valueOf(store.getId()))
                        .storeName(store.getStoreName())
                        .build())
                .toList();
    }

    // 요청사항 수정
    public RequestUpdateResDto update(UUID requestId, RequestUpdateReqDto req) {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());
        Request request = requestRdbRepository.findByIdAndStoreId(requestId, storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 요청사항이 없습니다."));
        request.updateRequest(req);

        return RequestUpdateResDto.builder()
                .id(request.getId())
                .name(request.getName())
                .build();
    }

    // 요청사항 삭제
    public void deleteRequest(UUID requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        Store store = storeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        long deleted = requestRdbRepository.deleteByIdAndStore_Id(requestId, store.getId());
    }
}

