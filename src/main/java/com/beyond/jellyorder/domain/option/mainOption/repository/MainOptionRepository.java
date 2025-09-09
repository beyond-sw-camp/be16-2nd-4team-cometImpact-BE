package com.beyond.jellyorder.domain.option.mainOption.repository;

import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MainOptionRepository extends JpaRepository<MainOption, UUID> {
    Optional<MainOption> findByMenu_IdAndNameAndDeletedFalse(UUID menuId, String name);
    List<MainOption> findAllByMenu_IdAndDeletedFalse(UUID menuId);
}
