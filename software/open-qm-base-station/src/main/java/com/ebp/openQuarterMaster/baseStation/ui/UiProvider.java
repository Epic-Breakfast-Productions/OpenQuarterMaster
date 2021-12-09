package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

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

    protected static List<NewCookie> refreshAuthToken(AuthMode authMode, KeycloakServiceCaller ksc, String refreshCode){
        //TODO:: this: https://stackoverflow.com/questions/51386337/refresh-access-token-via-refresh-token-in-keycloak
        return null;
    }

}
