package com.beyond.jellyorder.domain.sseRequest.repository;

import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import com.beyond.jellyorder.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {
    List<Request> findByStoreId(Store store); // or findByStore
}