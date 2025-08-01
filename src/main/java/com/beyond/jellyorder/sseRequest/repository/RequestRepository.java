package com.beyond.jellyorder.sseRequest.repository;

import com.beyond.jellyorder.sseRequest.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {
    List<Request> findByStoreId(String storeId);
}
