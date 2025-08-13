package com.beyond.jellyorder.common.auth;

import org.springframework.security.oauth2.jwt.Jwt;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class StoreTableJwtClaimUtil {
    public String getStoreId() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new IllegalStateException("요청 컨텍스트가 없습니다.");
        Object storeId = attrs.getRequest().getAttribute("storeId");
        if (storeId == null) throw new IllegalStateException("요청에 storeId 속성이 없습니다.");
        return storeId.toString();
    }

    public String getStoreTableId() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new IllegalStateException("요청 컨텍스트가 없습니다.");
        Object storeTableId = attrs.getRequest().getAttribute("storeTableId");
        if (storeTableId == null) throw new IllegalStateException("요청에 storeTableId 속성이 없습니다.");
        return storeTableId.toString();
    }
}

