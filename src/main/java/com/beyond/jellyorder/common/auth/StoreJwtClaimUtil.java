package com.beyond.jellyorder.common.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
*
* */
@Component
public class StoreJwtClaimUtil {
    public String getStoreId() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new IllegalStateException("요청 컨텍스트가 없습니다.");
        Object storeId = attrs.getRequest().getAttribute("storeId");
        if (storeId == null) throw new IllegalStateException("요청에 storeId 속성이 없습니다.");
        return storeId.toString();
    }
}
