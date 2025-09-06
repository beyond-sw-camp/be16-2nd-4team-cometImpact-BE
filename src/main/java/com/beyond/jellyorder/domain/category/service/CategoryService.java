package com.beyond.jellyorder.domain.category.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.category.dto.*;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final StoreJwtClaimUtil storeJwtClaimUtil;

    public CategoryCreateResDto create(CategoryCreateReqDto reqDto) {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        final String name = reqDto.getName();
        final String description = reqDto.getDescription();

        // 1) 살아있는 동일 이름 존재 → 중복
        if (categoryRepository.existsByStoreIdAndNameAndDeletedFalse(storeId, name)) {
            throw new DuplicateResourceException("이미 존재하는 카테고리입니다: " + name);
        }

        // 2) 삭제본이 있으면 가장 최근 1건만 복구
        var lastDeletedOpt = categoryRepository
                .findTopByStoreIdAndNameAndDeletedTrueOrderByDeletedAtDesc(storeId, name);

        if (lastDeletedOpt.isPresent()) {
            Category deletedOne = lastDeletedOpt.get();
            int restored = categoryRepository.restoreById(deletedOne.getId(), description);
            if (restored == 0) {
                throw new IllegalStateException("카테고리 복구에 실패했습니다.");
            }
            Category restoredCat = categoryRepository.findById(deletedOne.getId())
                    .orElseThrow(() -> new EntityNotFoundException("복구된 카테고리를 찾을 수 없습니다."));
            return new CategoryCreateResDto(restoredCat.getId(), restoredCat.getName(), restoredCat.getDescription());
        }

        // 3) 새로 생성
        Category saved = categoryRepository.save(Category.builder()
                .store(store)
                .name(name)
                .description(description)
                .build());

        return new CategoryCreateResDto(saved.getId(), saved.getName(), saved.getDescription());
    }

    @Transactional(readOnly = true)
    public List<GetCategoryResDto> getCategoriesByStore() {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());

        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 살아있는 카테고리만 조회
        List<Category> categoryList = categoryRepository.findAllByStoreIdAndDeletedFalse(storeId);

        return categoryList.stream()
                .map(c -> new GetCategoryResDto(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    public CategoryModifyResDto modifyCategory(CategoryModifyReqDto reqDto) {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());
        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 살아있는 카테고리만 조회
        Category category = categoryRepository.findByIdAndStoreIdAndDeletedFalse(
                        reqDto.getCategoryId(), storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다."));

        if (category.getDescription().equals(reqDto.getNewDescription())
                && category.getName().equals(reqDto.getNewName())) {
            throw new IllegalArgumentException("카테고리 혹은 설명 중 수정된 사안이 있어야 합니다.");
        }

        if (!category.getName().equals(reqDto.getNewName())) {
            // 살아있는 카테고리명 중복 방지
            if (categoryRepository.existsByStoreIdAndNameAndDeletedFalse(storeId, reqDto.getNewName())) {
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

    public void deleteCategory(String categoryName) {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());

        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // “삭제 안 된 메뉴” 남아있으면 금지
        if (menuRepository.existsAliveMenuByStoreIdAndCategoryName(storeId, categoryName)) {
            throw new IllegalArgumentException("해당 카테고리에 소속된 메뉴가 존재하여 삭제할 수 없습니다.");
        }

        int updated = categoryRepository.softDeleteByStoreIdAndName(storeId, categoryName);
        if (updated == 0) {
            throw new EntityNotFoundException("삭제 대상 카테고리를 찾을 수 없습니다.");
        }
    }
}
