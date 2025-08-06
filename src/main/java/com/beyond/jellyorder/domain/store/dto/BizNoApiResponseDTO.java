package com.beyond.jellyorder.domain.store.dto;

import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BizNoApiResponseDTO {
    private List<BizData> data;

    @Getter
    public static class BizData {
        private String b_no;       // 사업자등록번호
        private String b_stt;      // 상태 (예: 계속사업자, 폐업자)
        private String b_stt_cd;   // 상태 코드 ("01"이면 정상)
    }
}
