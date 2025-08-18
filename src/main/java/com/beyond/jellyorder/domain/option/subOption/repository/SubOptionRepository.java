package com.beyond.jellyorder.domain.option.subOption.repository;

import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubOptionRepository extends JpaRepository<SubOption, UUID> {
}
