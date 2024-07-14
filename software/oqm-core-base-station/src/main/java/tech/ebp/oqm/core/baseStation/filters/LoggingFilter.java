package tech.ebp.oqm.core.baseStation.filters;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Provider
public class LoggingFilter implements ContainerRequestFilter {
	
	@Context
	UriInfo uriInfo;
	
	@Context
	HttpServerRequest request;
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		log.info(
			"Incoming Request from {} - {}:{}  ssl?: {}  Headers ({}): {}",
			request.remoteAddress().toString(),
			requestContext.getMethod(),
			uriInfo.getPath(),
			request.isSSL(),
			request.headers().size(),
			request.headers()
		);
		
	}
}
