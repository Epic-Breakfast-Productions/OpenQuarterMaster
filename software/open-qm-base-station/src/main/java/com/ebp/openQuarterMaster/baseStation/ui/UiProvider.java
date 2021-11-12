package com.ebp.openQuarterMaster.baseStation.ui;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.SecurityContext;

@Slf4j
public abstract class UiProvider {
    protected static final String USER_INFO_DATA_KEY = "userInfo";

    protected static void logRequestContext(JsonWebToken jwt, SecurityContext context) {
        if (!hasJwt(jwt)) {
            log.info("Processing request with no JWT; ssh:{}", context.isSecure());
        } else {
            log.info(
                    "Processing request with JWT; User:{} ssh:{}",
                    context.getUserPrincipal().getName(),
                    context.isSecure()
            );
            if (context.isSecure()) {
                log.warn("Request with JWT made without HTTPS");
            }
        }
        log.debug("Raw jwt: {}", jwt.getRawToken());
    }

    protected static boolean hasJwt(JsonWebToken jwt) {
        return jwt != null && jwt.getClaimNames() != null;
    }

}
