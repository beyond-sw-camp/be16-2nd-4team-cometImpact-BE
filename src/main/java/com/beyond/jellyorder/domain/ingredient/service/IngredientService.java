package com.beyond.jellyorder.domain.ingredient.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.dto.*;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 식자재(Ingredient) 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * - 식자재 생성
 * - 중복 이름 검사
 * - 매장별 식자재 관리 등
 */
@Service
@Transactional
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final StoreRepository storeRepository;

    /**
     * 새로운 식자재를 생성한다.
     * - 동일 매장(storeId) 내에서 이름(name)이 중복되면 예외를 발생시킨다.
     * - 생성된 식자재를 저장 후 응답 DTO로 변환하여 반환한다.
     *
     * @param reqDto 클라이언트로부터 전달받은 식자재 생성 요청 DTO
     * @return 생성된 식자재 정보를 담은 응답 DTO
     * @throws DuplicateResourceException 동일 매장 내에 이름이 중복되는 경우
     */
    public IngredientCreateResDto create(IngredientCreateReqDto reqDto) {
    /*
        TODO: [2025-07-31 기준] storeId에 대한 실제 매장 UUID 유효성 검증 로직 추가 예정
    */

        if (ingredientRepository.existsByStoreIdAndName(reqDto.getStoreId(), reqDto.getName())) {
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }

        try {
            // 식자재 엔티티 생성
            Ingredient ingredient = Ingredient.builder()
                    .storeId(reqDto.getStoreId())
                    .name(reqDto.getName())
                    .status(reqDto.getStatus())
                    .build();

            // DB에 저장
            Ingredient saved = ingredientRepository.save(ingredient);

            // 응답 DTO 반환
            return IngredientCreateResDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .status(saved.getStatus())
                    .build();

        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 인한 DB 중복 제약 위반 시 예외 변환
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }
    }

    @Transactional(readOnly = true)
    public IngredientListResDto getIngredientsByStoreId(String storeId) {
        // 1) storeId 유효성 검증 TODO: 추후 활성화 예정
        //        boolean storeExists = storeRepository.existsById(UUID.fromString(storeId));
        //        if (!storeExists) {
        //            throw new EntityNotFoundException("존재하지 않는 storeId입니다: " + storeId);
        //        }

        // 2) 원재료 조회
        List<Ingredient> ingredients = ingredientRepository.findAllByStoreId(storeId);

        // 3) DTO 변환 (재료가 없으면 빈 리스트)
        List<IngredientResDto> dtos = ingredients.stream()
                .map(i -> IngredientResDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .status(i.getStatus())
                        .build())
                .toList();

        // 4) 결과 반환
        return IngredientListResDto.builder()
                .ingredients(dtos)
                .build();
    }

    public IngredientDeleteResDto delete(IngredientDeleteReqDto req) {
        // 1) 대상 조회
        Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "식자재를 찾을 수 없습니다. id=" + req.getIngredientId()));

        // 2) 소속(storeId) 검증
        if (!ingredient.getStoreId().equals(req.getStoreId())) {
            throw new EntityNotFoundException(
                    "요청한 매장에 속하지 않는 식자재입니다. id=" + req.getIngredientId()
                            + ", storeId=" + req.getStoreId());
        }

        // 3) 영향받는 메뉴 사전 조회 (네이티브: BIN_TO_UUID → String)
        var briefs = ingredientRepository.findAffectedMenus(ingredient.getId());
        var affected = briefs.stream()
                .map(b -> IngredientDeleteResDto.AffectedMenuDto.builder()
                        .id(UUID.fromString(b.getId())) // String → UUID 변환
                        .name(b.getName())
                        .build())
                .toList();

        // 4) 삭제 (JPA orphanRemoval 또는 DB FK ON DELETE CASCADE로 연결행 정리)
        ingredientRepository.delete(ingredient);

        // 5) 응답 조립
        return IngredientDeleteResDto.builder()
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .affectedMenus(affected)
                .build();
    }

    public IngredientModifyResDto modify(String storeId, UUID ingredientId, IngredientModifyReqDto req) {

        if (req.getName() == null && req.getStatus() == null) {
            throw new IllegalArgumentException("수정할 필드가 없습니다. name 또는 status 중 최소 한 개는 필요합니다.");
        }

        Ingredient ingredient = ingredientRepository.findByIdAndStoreId(ingredientId, storeId)
                .orElseThrow(() -> new EntityNotFoundException("식자재를 찾을 수 없습니다. id=" + ingredientId + ", storeId=" + storeId));

        // 이름 수정
        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("식자재명은 공백일 수 없습니다.");
            }
            // 동일 매장 내 중복 방지
            boolean duplicated = ingredientRepository
                    .existsByStoreIdAndNameAndIdNot(storeId, newName, ingredientId);
            if (duplicated) {
                throw new IllegalArgumentException("동일 매장 내 이미 존재하는 식자재명입니다: " + newName);
            }
            ingredient.setName(newName);
        }

        // 상태 수정
        if (req.getStatus() != null) {
            ingredient.setStatus(req.getStatus());
        }

        Ingredient saved = ingredientRepository.save(ingredient);

        return IngredientModifyResDto.builder()
                .id(saved.getId())
                .storeId(saved.getStoreId())
                .name(saved.getName())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
