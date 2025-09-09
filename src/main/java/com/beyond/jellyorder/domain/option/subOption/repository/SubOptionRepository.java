package com.beyond.jellyorder.domain.option.subOption.repository;

import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubOptionRepository extends JpaRepository<SubOption, UUID> {
    List<SubOption> findAllByMainOption_IdAndDeletedFalseOrderByIdAsc(UUID mainOptionId);
}
