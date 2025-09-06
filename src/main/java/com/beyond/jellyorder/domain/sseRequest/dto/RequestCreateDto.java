package com.beyond.jellyorder.domain.sseRequest.dto;

//import com.beyond.jellyorder.domain.sseRequest.entity.Request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
// 고객 -> 점주, 실시간 요청용
public class RequestCreateDto implements Serializable {
    private UUID id;

    @NotBlank
    private String storeId;

    private String tableId;

    private String tableName;

    @Valid
    @NotEmpty
    private List<RequestItem> requests;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestItem implements Serializable {
        @NotBlank
        private String name;

        private Integer quantity;

        /** 수량 가산(멱등 병합 시 사용) */
        public RequestItem addQuantity(int delta) {
            if (delta > 0) this.quantity = (this.quantity == null ? delta : this.quantity + delta);
            return this;
        }
    }

    /** 같은 name 병합(+수량 보정) */
    public RequestCreateDto normalizeAndMerge() {
        if (requests == null) requests = new ArrayList<>();
        Map<String, RequestItem> merged = new LinkedHashMap<>();
        for (RequestItem it : requests) {
            if (it == null || it.getName() == null || it.getName().isBlank()) continue;
            int q = (it.getQuantity() == null || it.getQuantity() <= 0) ? 1 : it.getQuantity();
            merged.compute(it.getName(), (k, prev) ->
                    prev == null ? RequestItem.builder().name(k).quantity(q).build()
                            : prev.addQuantity(q));
        }
        requests = new ArrayList<>(merged.values());
        return this;
    }

    public RequestCreateDto assignId(UUID newId) { this.id = newId; return this; }
}
