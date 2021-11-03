package com.ebp.openQuarterMaster.baseStation.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.SecurityContext;

@Slf4j
public abstract class EndpointProvider {

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

    }

    protected static boolean hasJwt(JsonWebToken jwt) {
        return jwt != null && jwt.getClaimNames() != null;
    }

}
