package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreTableLoginReqDTO {
//    점주의 로그인 아이디
    @NotBlank(message = "점주의 로그인 아이디는 필수 입력값입니다.")
    private String loginId;
//    점주의 비밀번호
    @NotBlank(message = "점주의 비밀번호는 필수 입력값입니다.")
    private String password;
    @NotBlank(message = "테이블 이름은 필수 입력값입니다.")
    private String name;

}


