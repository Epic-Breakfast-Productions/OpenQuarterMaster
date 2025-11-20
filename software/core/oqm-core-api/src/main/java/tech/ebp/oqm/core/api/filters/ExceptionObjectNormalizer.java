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
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import tech.ebp.oqm.core.api.model.rest.ErrorMessage;
import tech.ebp.oqm.core.api.exception.db.DbModValidationException;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.utils.UrlUtils;

import java.io.IOException;
import java.util.Iterator;

@Slf4j
@Provider
public class ExceptionObjectNormalizer implements ContainerResponseFilter {
	
	@Context
	UriInfo uriInfo;
	
	private ErrorMessage.Builder<?, ?> buildOutputForHibViolation(ViolationReport report) {
		log.debug("Violation Report type. Mapping to standard error object.");
		
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
		
		//TODO:: add original obj?
		return ErrorMessage.builder()
							.displayMessage(sb.toString())
							.cause(report);
	}
	
	private int getStatusCodeForException(Throwable e) {
		return switch (e){
			case DbModValidationException ignored -> 400;
			case DbNotFoundException ignored -> 404;
			default -> 500;
		};
	}
	
	/**
	 *
	 * @param requestContext request context.
	 * @param responseContext response context.
	 * @throws IOException
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Response.Status.Family responseFam = responseContext.getStatusInfo().getFamily();
		if (
			UrlUtils.isUiEndpoint(uriInfo) ||
			(
				responseFam != Response.Status.Family.CLIENT_ERROR &&
				responseFam != Response.Status.Family.SERVER_ERROR
				)
		) {//not an error
			return;
		}
		if (responseContext.getEntity() instanceof ErrorMessage) {
			log.debug("Response was already an Error Message: {}",  responseContext.getEntity());
			return;
		}
		
		ErrorMessage.Builder<?, ?> outputBuilder = null;
		log.debug("Type of response object: {}", responseContext.getEntityType());
		
		if (responseContext.getEntity() instanceof ViolationReport) {
			outputBuilder = buildOutputForHibViolation((ViolationReport) responseContext.getEntity());
		} else if (responseContext.getEntity() instanceof Throwable) { // sometimes (or maybe never?) present
			Throwable e = (Throwable) responseContext.getEntity();
			
			outputBuilder = ErrorMessage.builder()
								.displayMessage(e.getMessage());
			responseContext.setStatus(getStatusCodeForException(e));
		} else { // nothing left to go on, entity or exception-wise
			log.warn("Unknown response type: {} / {} / {}", responseContext.getEntityType(), responseContext.getEntity(), responseContext.getStatusInfo().getReasonPhrase());
			
			outputBuilder = ErrorMessage.builder()
								.generic(true);
			if(responseFam == Response.Status.Family.CLIENT_ERROR){
				switch (responseContext.getStatus()) {
					case 401:
					case 403:
					case 404:
					case 405:
					case 406:
						//specific enough to be generic
						outputBuilder.displayMessage(responseContext.getStatusInfo().getReasonPhrase()).generic(false);
						break;
					default:
						outputBuilder
							.displayMessage("Unknown client error occurred: "+responseContext.getStatusInfo().getReasonPhrase()+" This might be caused by bad data read by the server.");
						break;
				}
				
			} else if(responseFam == Response.Status.Family.SERVER_ERROR) {
				outputBuilder
					.displayMessage("Unknown server error occurred.");
			}
		}
		
		if (outputBuilder != null) {
			ErrorMessage message = outputBuilder.build();
			log.debug("Error message: {}", message);
			
			responseContext.setEntity(message);
		} else {
			log.warn("No error message builder found for error response: {}", responseContext);
		}
	}
	
	@ServerExceptionMapper
	public RestResponse<ErrorMessage> mapException(DbModValidationException x) {
		return RestResponse.status(
			Response.Status.BAD_REQUEST,
			ErrorMessage.builder()
				.displayMessage(x.getMessage())
				.build()
		);
	}
	
	//TODO:: mappers for other exceptions
}
