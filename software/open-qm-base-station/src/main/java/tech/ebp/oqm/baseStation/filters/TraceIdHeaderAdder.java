package tech.ebp.oqm.baseStation.filters;

import io.opentracing.Tracer;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Iterator;

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
		
		log.debug("Type of response: " + responseContext.getEntityType());
		
		if(responseContext.getEntity() instanceof ViolationReport){
			log.info("Violation Report type. Mapping to standard error.");
			ViolationReport report = (ViolationReport) responseContext.getEntity();
			
			StringBuilder sb = new StringBuilder("Data validation errors ("+report.getViolations().size()+"): ");
			
			Iterator<ViolationReport.Violation> it = report.getViolations().iterator();
			while (it.hasNext()){
				ViolationReport.Violation violation = it.next();
				String field;
				{
					String[] fieldParts = violation.getField().split("\\.");
					field = fieldParts[fieldParts.length - 1];
				}
				sb.append(field + "- " + violation.getMessage());
				
				if(it.hasNext()){
					sb.append(", ");
				}
			}
			
			ErrorMessage.Builder<?, ?> outputBuilder = ErrorMessage.builder()
														   .displayMessage(sb.toString());
			
			responseContext.setEntity(outputBuilder.build());
		}
		
		responseContext.getHeaders().add("traceId", traceId);
	}
}
