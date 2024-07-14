package tech.ebp.oqm.core.api.filters;


import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.rest.ErrorMessage;
import tech.ebp.oqm.core.api.utils.UrlUtils;

import java.io.IOException;
import java.util.Iterator;

@Slf4j
@Provider
public class ExceptionObjectNormalizer implements ContainerResponseFilter {
	
	@Context
	UriInfo uriInfo;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		if (UrlUtils.isUiEndpoint(uriInfo)) {
			return;
		}
		
		ErrorMessage.Builder<?, ?> outputBuilder = null;
		log.debug("Type of response object: " + responseContext.getEntityType());
		
		if (responseContext.getEntity() instanceof ViolationReport) {
			log.info("Violation Report type. Mapping to standard error.");
			ViolationReport report = (ViolationReport) responseContext.getEntity();
			
			StringBuilder sb = new StringBuilder("Data validation errors (" + report.getViolations().size() + "): ");
			
			Iterator<io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport.Violation> it = report.getViolations().iterator();
			while (it.hasNext()) {
				io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport.Violation violation = it.next();
				String field;
				{
					//TODO:: account for sub-fields
					String[] fieldParts = violation.getField().split("\\.");
					field = fieldParts[fieldParts.length - 1];
				}
				sb.append(field + "- " + violation.getMessage());
				
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			
			//TODO:: add original obj
			outputBuilder = ErrorMessage.builder()
										.displayMessage(sb.toString())
										.cause(report);
		} else if (responseContext.getEntity() instanceof Throwable) {
			Throwable e = (Throwable) responseContext.getEntity();
			
			//TODO:: account for different exceptions?
			outputBuilder = ErrorMessage.builder()
										.displayMessage(e.getMessage())
										.cause(e);
		}
		
		if (outputBuilder != null) {
			ErrorMessage message = outputBuilder.build();
			
			log.info("Error cause type: {}", message.getCause().getClass().getCanonicalName());
			
			responseContext.setEntity(message);
		}
	}
}
