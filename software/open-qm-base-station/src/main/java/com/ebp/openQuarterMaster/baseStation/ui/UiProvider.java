package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.SecurityContext;

import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

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

    protected static JsonNode refreshAuthToken(KeycloakServiceCaller ksc, String refreshCode) {
        if (!EXTERNAL.equals(ConfigProvider.getConfig().getValue("service.authMode", AuthMode.class))) {
            return null;
        }
        if(refreshCode == null || refreshCode.isBlank()){
            return null;
        }

        JsonNode response;

        try {
            response = ksc.refreshToken(
                    ConfigProvider.getConfig().getValue("service.externalAuth.clientId", String.class),
                    ConfigProvider.getConfig().getValue("service.externalAuth.clientSecret", String.class),
                    "refresh_token",
                    refreshCode
            );
        } catch (Throwable e) {
            log.warn("Failed to refresh token from keycloak (exception)- ", e);
            //TODO:: deal with properly
            e.printStackTrace();
            throw e;
        }

        log.info("Got response from keycloak on token refresh request: {}", response);

        return response;
    }

}
