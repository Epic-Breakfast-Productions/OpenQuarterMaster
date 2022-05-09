package com.ebp.openQuarterMaster.baseStation.filters;

import io.opentracing.Tracer;
import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Slf4j
@Provider
public class LoggingFilter implements ContainerResponseFilter, ContainerRequestFilter {
	
	@Context
	UriInfo uriInfo;
	
	@Context
	HttpServerRequest request;
	
	@Inject
	Tracer tracer;
	
	@ConfigProperty(name = "quarkus.jaeger.service-name")
	String serviceId;
	
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
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		log.info(
			"Response:: Type: {}  status: {}",
			responseContext.getMediaType(),
			responseContext.getStatus()
		);
		responseContext.getHeaders().add("serviceId", this.serviceId);
		responseContext.getHeaders().add("traceId", this.tracer.activeSpan().context().toTraceId());
	}
	
}
