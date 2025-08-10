package com.beyond.jellyorder.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 국세청 사업자등록 진위확인 API 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BizNoApiResDTO(
        @JsonProperty("match_cnt")   Integer matchCnt,
        @JsonProperty("request_cnt") Integer requestCnt,
        List<Item> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("b_no")      String bNo,
            String valid,                  // "01" = 일치, "02" = 불일치
            @JsonProperty("valid_msg") String validMsg,
            @JsonProperty("request_param") RequestParam requestParam,
            Status status
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RequestParam(
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(
            @JsonProperty("b_no")        String bNo,
            @JsonProperty("b_stt")       String bStt,      // 예: "계속사업자", "폐업자"
            @JsonProperty("b_stt_cd")    String bSttCd,    // "01" 정상, "02" 폐업, "03" 휴업
            @JsonProperty("tax_type")    String taxType,
            @JsonProperty("tax_type_cd") String taxTypeCd,
            @JsonProperty("end_dt")      String endDt,
            @JsonProperty("utcc_yn")     String utccYn,
            @JsonProperty("tax_type_change_dt") String taxTypeChangeDt,
            @JsonProperty("invoice_apply_dt")   String invoiceApplyDt,
            @JsonProperty("rbf_tax_type")       String rbfTaxType,
            @JsonProperty("rbf_tax_type_cd")    String rbfTaxTypeCd
    ) { }
}