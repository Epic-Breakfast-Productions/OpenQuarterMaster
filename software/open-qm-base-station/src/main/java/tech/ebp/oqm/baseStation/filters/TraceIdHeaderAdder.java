package tech.ebp.oqm.baseStation.filters;

import io.opentracing.Tracer;
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
	Tracer tracer;
	
	@ConfigProperty(name = "quarkus.jaeger.service-name")
	String serviceId;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		responseContext.getHeaders().add("serviceId", this.serviceId);
		
		String traceId = "";
		if(this.tracer.activeSpan() != null) {
			traceId = this.tracer
				.activeSpan()
				.context()
				.toTraceId();
		}
		responseContext.getHeaders().add("traceId", traceId);
	}
}
