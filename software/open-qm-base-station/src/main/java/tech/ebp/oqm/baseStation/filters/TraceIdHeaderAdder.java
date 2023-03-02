package tech.ebp.oqm.baseStation.filters;

import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Slf4j
@Provider
public class TraceIdHeaderAdder implements ContainerResponseFilter {
	
	@Inject
	Span span;
	
	@ConfigProperty(name = "quarkus.application.name")
	String serviceId;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		responseContext.getHeaders().add("serviceId", this.serviceId);
		
		String traceId = "";
		if(this.span.getSpanContext().getTraceId() != null) {
			traceId = this.span.getSpanContext().getTraceId();
		}
		
		responseContext.getHeaders().add("traceId", traceId);
	}
}
