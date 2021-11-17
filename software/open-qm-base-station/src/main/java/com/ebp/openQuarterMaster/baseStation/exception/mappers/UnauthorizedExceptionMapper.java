package com.ebp.openQuarterMaster.baseStation.exception.mappers;

import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.URI;

@Slf4j
@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
    //you can inject JAXRS contextual resources here
    @Context
    UriInfo crc;
    //    @Context
//    Cookie cookie;
    @Context
    JsonWebToken jsonWebToken;

    private static boolean isUiEndpoint(URI uri) {
        String path = uri.getPath();

        return !path.startsWith("/api") &&
                !path.startsWith("/q/") &&
                !path.startsWith("/openapi");
    }

    @Override
    public Response toResponse(UnauthorizedException exception) {
        log.warn("User not authorized to access: {} - {}", crc.getRequestUri(), exception.getMessage());
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
                    .build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }


}
