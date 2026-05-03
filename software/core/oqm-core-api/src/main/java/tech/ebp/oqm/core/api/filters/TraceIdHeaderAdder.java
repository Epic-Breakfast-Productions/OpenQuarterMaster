//package tech.ebp.oqm.core.api.filters;
//
//import jakarta.inject.Inject;
//import jakarta.ws.rs.container.ContainerRequestContext;
//import jakarta.ws.rs.container.ContainerResponseContext;
//import jakarta.ws.rs.container.ContainerResponseFilter;
//import jakarta.ws.rs.ext.Provider;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.microprofile.config.inject.ConfigProperty;
//
//import java.io.IOException;
//
//@Slf4j
//@Provider
//public class TraceIdHeaderAdder implements ContainerResponseFilter {
//
//	@Inject
//	Span span;
//
//	@ConfigProperty(name = "quarkus.application.name")
//	String serviceId;
//
//	@Override
//	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
//		responseContext.getHeaders().add("serviceId", this.serviceId);
//
//		String traceId = "";
//		if(this.span.getSpanContext().getTraceId() != null) {
//			traceId = this.span.getSpanContext().getTraceId();
//		}
//
//		responseContext.getHeaders().add("traceId", traceId);
//	}
//}
