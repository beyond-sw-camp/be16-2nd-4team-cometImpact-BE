package com.beyond.jellyorder.domain.category.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.category.dto.CategoryCreateReqDto;
import com.beyond.jellyorder.domain.category.dto.CategoryCreateResDto;
import com.beyond.jellyorder.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 관련 요청을 처리하는 REST 컨트롤러 클래스.
 * 클라이언트로부터 전달된 카테고리 생성 요청을 받아 서비스 계층에 위임하고,
 * 결과를 표준 응답 형식으로 반환한다.
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리를 생성하는 API 엔드포인트.
     * 클라이언트로부터 전달된 JSON 요청 본문을 DTO로 변환하고, 유효성 검사를 수행한다.
     * 이후 서비스 계층을 통해 카테고리를 저장하고, 결과를 포함한 표준 응답을 반환한다.
     *
     * @param reqDto 카테고리 생성 요청 데이터 (storeId(추후 삭제 예정), name, description 포함)
     * @return 생성된 카테고리 정보와 성공 메시지를 포함한 응답
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @RequestBody @Valid CategoryCreateReqDto reqDto
    ) {
        // 서비스 계층을 통해 카테고리 생성 처리
        CategoryCreateResDto resDto = categoryService.create(reqDto);

        // 표준 API 응답 포맷으로 반환 (201 Created)
        return ApiResponse.created(resDto, reqDto.getName() + " 카테고리가 정상적으로 저장되었습니다.");
    }
}
