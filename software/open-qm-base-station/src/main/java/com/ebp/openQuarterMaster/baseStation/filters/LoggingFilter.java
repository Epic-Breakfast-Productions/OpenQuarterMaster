package com.ebp.openQuarterMaster.baseStation.filters;

import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
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
