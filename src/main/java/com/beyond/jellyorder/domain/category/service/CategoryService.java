package com.beyond.jellyorder.domain.category.service;

import com.beyond.jellyorder.domain.category.dto.CategoryCreateReqDto;
import com.beyond.jellyorder.domain.category.dto.CategoryCreateResDto;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.category.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카테고리 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * 카테고리 생성 요청을 처리하며, 동일 storeId 내 중복된 카테고리명 존재 여부를 검증한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 새로운 카테고리를 생성한다.
     * 동일 storeId 내에서 name이 중복될 경우 예외를 발생시킨다.
     *
     * @param categoryCreateReqDto 클라이언트로부터 전달받은 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 정보를 담은 응답 DTO
     * @throws DuplicateCategoryNameException 동일한 이름의 카테고리가 이미 존재하는 경우 발생
     */
    public CategoryCreateResDto create(CategoryCreateReqDto dto) {
        // TODO [2025-07-31]: storeId 유효성 검증 (인증된 점주의 storeId인지 확인)

        if (categoryRepository.existsByStoreIdAndName(dto.getStoreId(), dto.getName())) {
            throw new DuplicateCategoryNameException(dto.getName());
        }

        Category saved = categoryRepository.save(Category.builder()
                .storeId(dto.getStoreId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build());

        return new CategoryCreateResDto(saved.getId(), saved.getName(), saved.getDescription());
    }

    /**
     * 동일한 storeId 범위 내에 이미 동일한 name 값을 갖는 카테고리가 존재하는 경우 발생하는 예외이다.
     * 서비스 계층에서 중복 카테고리 등록을 사전에 방지하기 위한 목적으로 사용된다.
     * 현재는 CategoryService의 내부 static 클래스로 정의되어 있으나,
     * 향후 예외 처리 일관성을 위해 공통 예외 처리 패키지로의 분리가 검토가 필요하다.
     */
    public static class DuplicateCategoryNameException extends RuntimeException {
        public DuplicateCategoryNameException(String name) {
            super("이미 존재하는 카테고리입니다: " + name);
        }
    }
}
