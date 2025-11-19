package tech.ebp.oqm.core.api.filters;


import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
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
	
	private ErrorMessage.Builder<?, ?> buildOutputForHibViolation(ViolationReport report) {
		log.info("Violation Report type. Mapping to standard error object.");
		
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
		return ErrorMessage.builder()
							.displayMessage(sb.toString())
							.cause(report);
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Response.Status.Family responseFam = responseContext.getStatusInfo().getFamily();
		if (
			UrlUtils.isUiEndpoint(uriInfo) ||
			(
				responseFam != Response.Status.Family.CLIENT_ERROR &&
				responseFam != Response.Status.Family.SERVER_ERROR
				)
		) {
			return;
		}
		
		ErrorMessage.Builder<?, ?> outputBuilder = null;
		log.debug("Type of response object: {}", responseContext.getEntityType());
		
		
		if (responseContext.getEntity() instanceof ViolationReport) {
			outputBuilder = buildOutputForHibViolation((ViolationReport) responseContext.getEntity());
		} else if (responseContext.getEntity() instanceof Throwable) {
			Throwable e = (Throwable) responseContext.getEntity();
			
			//TODO:: account for different exceptions?
			outputBuilder = ErrorMessage.builder()
										.displayMessage(e.getMessage())
										.cause(e);
		} else {
			log.warn("Unknown response type: {} / {}", responseContext.getEntityType(), responseContext.getEntity());
			
			if(responseFam == Response.Status.Family.CLIENT_ERROR){
				outputBuilder = ErrorMessage.builder()
									.displayMessage("Unknown client error occurred. This might be caused by bad data read by the server.");
			} else if(responseFam == Response.Status.Family.SERVER_ERROR) {
				outputBuilder = ErrorMessage.builder()
									.displayMessage("Unknown server error occurred.");
			}
		}
		
		if (outputBuilder != null) {
			ErrorMessage message = outputBuilder.build();
			
			log.info("Error message: {}", message);
			
			responseContext.setEntity(message);
		} else {
			log.warn("No error message builder found for error response: {}", responseContext);
		}
	}
}
