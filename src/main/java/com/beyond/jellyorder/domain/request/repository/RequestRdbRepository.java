package com.beyond.jellyorder.domain.request.repository;

import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import com.beyond.jellyorder.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRdbRepository extends JpaRepository<Request, UUID> {
    List<Request> findAllByStoreId(Store store);
    // 테스트용
    List<Request> findAllByStore(Store store);
}
