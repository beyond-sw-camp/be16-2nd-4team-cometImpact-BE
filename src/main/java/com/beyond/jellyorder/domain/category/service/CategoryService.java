package com.beyond.jellyorder.domain.category.service;

import com.beyond.jellyorder.domain.category.dto.*;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.category.domain.Category;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beyond.jellyorder.common.exception.DuplicateResourceException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * @param reqDto 클라이언트로부터 전달받은 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 정보를 담은 응답 DTO
     * @throws DuplicateResourceException 동일한 이름의 카테고리가 이미 존재하는 경우 발생
     */

    public CategoryCreateResDto create(CategoryCreateReqDto reqDto) {
        // TODO [2025-07-31]: storeId 유효성 검증 (인증된 점주의 storeId인지 확인)

        if (categoryRepository.existsByStoreIdAndName(reqDto.getStoreId(), reqDto.getName())) {
            throw new DuplicateResourceException("이미 존재하는 카테고리입니다: " + reqDto.getName());
        }

        Category saved = categoryRepository.save(Category.builder()
                .storeId(reqDto.getStoreId())
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .build());

        return new CategoryCreateResDto(saved.getId(), saved.getName(), saved.getDescription());
    }

    public List<GetCategoryResDto> getCategoriesByStore(String storeId) {
        // TODO [2025-08-02]: storeId 유효성 검증 (인증된 점주의 storeId인지 확인)

        if (storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("storeId는 null이거나 공백일 수 없습니다.");
        }

        List<Category> categoryList = categoryRepository.findAllByStoreId(storeId);

        if (categoryList.isEmpty()) {
            throw new EntityNotFoundException("해당 storeId에 대한 카테고리가 존재하지 않습니다: " + storeId);
        }

        return categoryList.stream()
                .map(category -> new GetCategoryResDto(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    public CategoryModifyResDto modifyCategory(CategoryModifyReqDto reqDto) {
        // 이후 Authentication 객체 내에서 storeId 검증 추가 예정
        Category category = categoryRepository.findByIdAndStoreId(reqDto.getCategoryId(), reqDto.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다."));

        if(category.getDescription().equals(reqDto.getNewDescription()) && category.getName().equals(reqDto.getNewName())) {
            throw new IllegalArgumentException("카테고리 혹은 설명 중 수정된 사안이 있어야 합니다.");
        }

        if (!category.getName().equals(reqDto.getNewName())) {
            if (categoryRepository.existsByStoreIdAndName(reqDto.getStoreId(), reqDto.getNewName())) {
                throw new DuplicateResourceException("이미 존재하는 카테고리명입니다: " + reqDto.getNewName());
            }
            category.setName(reqDto.getNewName());
        }

        if (!category.getDescription().equals(reqDto.getNewDescription())) {
            category.setDescription(reqDto.getNewDescription());
        }

        return CategoryModifyResDto.builder()
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}