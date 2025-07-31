package com.beyond.jellyorder.domain.ingredient.service;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientCreateReqDto;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientCreateResDto;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 새로운 식자재를 생성한다.
     * - 동일 매장(storeId) 내에서 이름(name)이 중복되면 예외를 발생시킨다.
     * - 생성된 식자재를 저장 후 응답 DTO로 변환하여 반환한다.
     *
     * @param reqDto 클라이언트로부터 전달받은 식자재 생성 요청 DTO
     * @return 생성된 식자재 정보를 담은 응답 DTO
     * @throws DuplicateIngredientNameException 동일 매장 내에 이름이 중복되는 경우
     */
    public IngredientCreateResDto create(IngredientCreateReqDto reqDto) {
    /*
        TODO: [2025-07-31 기준] storeId에 대한 실제 매장 UUID 유효성 검증 로직 추가 예정
    */

        // 1차 중복 검사
        boolean exists = ingredientRepository.existsByStoreIdAndName(reqDto.getStoreId(), reqDto.getName());
        if (exists) {
            throw new DuplicateIngredientNameException(reqDto.getName());
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
            throw new DuplicateIngredientNameException(reqDto.getName());
        }
    }

    /**
     * 동일한 이름의 식자재가 이미 존재하는 경우 발생하는 예외 클래스.
     * - 매장 단위 중복 등록 방지 목적
     * - 전역 예외 처리 핸들러에서 처리 가능
     */
    public static class DuplicateIngredientNameException extends RuntimeException {
        public DuplicateIngredientNameException(String name) {
            super("이미 존재하는 식자재입니다: " + name);
        }
    }


}
