package com.beyond.jellyorder.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 국세청 사업자등록 진위확인 API 요청 DTO
 * {
 *   "businesses": [
 *     { "b_no": "...", "start_dt": "...", "p_nm": "...", ... }
 *   ]
 * }
 */
public record BizNoApiReqDTO(
        @JsonProperty("businesses")
        List<Business> businesses
) {
    public static BizNoApiReqDTO ofSingle(String bNo, String startDt, String pNm) {
        return new BizNoApiReqDTO(List.of(new Business(
                bNo, startDt, pNm,
                "", "", "", "", "", ""
        )));
    }

    public record Business(
            @JsonProperty("b_no")     String bNo,
            @JsonProperty("start_dt") String startDt,
            @JsonProperty("p_nm")     String pNm,
            @JsonProperty("p_nm2")    String pNm2,
            @JsonProperty("b_nm")     String bNm,
            @JsonProperty("corp_no")  String corpNo,
            @JsonProperty("b_sector") String bSector,
            @JsonProperty("b_type")   String bType,
            @JsonProperty("b_adr")    String bAdr
    ) { }
}