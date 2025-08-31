package com.beyond.jellyorder.domain.request.repository;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import com.beyond.jellyorder.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRdbRepository extends JpaRepository<Request, UUID> {
    // 요청사항 목록 조회용
    List<Request> findAllByStore(Store store);

    // 요청사항 수정용
    Optional<Request> findByIdAndStoreId(UUID id, UUID store_id);

    // 요청사항 삭제용
    long deleteByIdAndStore_Id(UUID requestId, UUID storeId);
}
