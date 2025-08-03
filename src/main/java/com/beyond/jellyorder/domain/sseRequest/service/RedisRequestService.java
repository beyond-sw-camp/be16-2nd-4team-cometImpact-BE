package com.beyond.jellyorder.sseRequest.service;

import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.sseRequest.dto.RequestCreateDto;
import com.beyond.jellyorder.sseRequest.repository.RequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Redis에 실시간 요청 저장,조회,삭제
public class RedisRequestService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StoreRepository storeRepository;

    // 필드 및 Redis 키 정의(순서와 데이터 조회를 동시에 보장)
    private static final String REQUEST_LIST_KEY = "request:list";  // 요청 ID들을 순서대로 저장하는 List 구조의 키
    private static final String REQUEST_HASH_KEY = "request:data";  // 각 요청 ID별로 상세 데이터를 저장하는 Hash 구조의 키

    // 고객 요청을 redis에 저장
    public void save(RequestCreateDto dto) {

        // storeId 입력 여부 판단
        if (dto.getStoreId() == null || dto.getStoreId().isBlank()) {
            throw new IllegalArgumentException("storeId는 비어 있을 수 없습니다.");
        }

        // storeId 존재 여부 판단
        boolean storeExists = storeRepository.existsById(UUID.fromString(dto.getStoreId()));

        if (!storeExists) {
            throw new EntityNotFoundException("해당 storeId는 존재하지 않습니다.");
        }

        // 중복된 요청 ID 확인
        if (dto.getId() != null) {
            boolean isRequestIdDuplicate = redisTemplate.opsForHash()
                    .hasKey(REQUEST_HASH_KEY, dto.getId().toString());
            if (isRequestIdDuplicate) {
                throw new IllegalArgumentException("중복된 요청 ID입니다.");
            }
        }

        UUID id = UUID.randomUUID();
        dto.setId(id);
        // redis에 저장
        redisTemplate.opsForHash().put(REQUEST_HASH_KEY, String.valueOf(id), dto);  // 요청 데이터를 Hash에 저장 (id -> dto)
        redisTemplate.opsForList().rightPush(REQUEST_LIST_KEY, String.valueOf(id)); // 요청 ID를 List에 추가 (순서 보장)
    }

    // 점주가 현재 들어온 요청 목록을 조회
    public List<RequestCreateDto> getAll() {
        List<Object> requestIds = redisTemplate.opsForList().range(REQUEST_LIST_KEY, 0, -1);   // Redis List에서 모든 요청 ID를 꺼냄
        List<RequestCreateDto> result = new ArrayList<>();
        for (Object idObj : requestIds) {      // 요청 순서를 유지한 상태로 List에 담아 반환
            RequestCreateDto dto = (RequestCreateDto) redisTemplate.opsForHash().get(REQUEST_HASH_KEY, idObj.toString());
            if (dto != null) result.add(dto);
        }
        return result;
    }

    // 점주가 요청을 처리 시 해당 요청 삭제
    public void delete(UUID requestId) {
        redisTemplate.opsForList().remove(REQUEST_LIST_KEY, 0, requestId.toString());  // List에서 해당 ID 제거
        redisTemplate.opsForHash().delete(REQUEST_HASH_KEY, requestId.toString()); // Hash에서 해당 ID에 대응하는 요청 데이터 삭제
    }
}
