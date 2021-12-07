package com.ebp.openQuarterMaster.baseStation.exception.mappers;

import com.ebp.openQuarterMaster.baseStation.ui.UiUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;

@Slf4j
public abstract class UiNotAuthorizedExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    protected static boolean isUiEndpoint(URI uri) {
        String path = uri.getPath();

        return !path.startsWith("/api") &&
                !path.startsWith("/q/") &&
                !path.startsWith("/openapi");
    }

    @Context
    UriInfo crc;

    @Context
    JsonWebToken jsonWebToken;

//    @Context
//    @CookieParam("jwt")
//    Map<String, Cookie> authCookies;

    public Response toResponse(E e) {
        log.warn("User not authorized to access: {} - {}/{}", crc.getRequestUri(), e.getClass().getName(), e.getMessage());

//        log.info("Cookie: {}", authCookies);
        URI uri = this.crc.getRequestUri();
        if (isUiEndpoint(uri)) {
            return Response.seeOther( //seeOther = 303 redirect
                            UriBuilder.fromUri("/")
                                    .queryParam("messageHeading", "Unauthorized")
                                    .queryParam("message", "Please login to access this page.")
                                    .queryParam("messageType", "danger")
                                    .queryParam("returnPath", uri.getPath() + (uri.getQuery() == null ? "" : "?" + uri.getQuery()))
                                    .build()
                    )//build the URL where you want to redirect
//                    .entity("Not authorized")//entity is not required
                    .cookie(
                            UiUtils.getAuthRemovalCookie()
                    )
                    .build();
        }
        log.info("Erring exception for url that wasn't in ui");
        return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage() + e.toString()).build();
    }
}
