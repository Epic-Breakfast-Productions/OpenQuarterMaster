package tech.ebp.oqm.baseStation.exception.mappers.apiAuth;

import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.exception.mappers.AwareExceptionMapper;
import tech.ebp.oqm.baseStation.interfaces.ui.UiUtils;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Slf4j
public abstract class UiNotAuthorizedExceptionMapper<E extends Throwable> extends AwareExceptionMapper<E> {
	
	@Context
	JsonWebToken jsonWebToken;
	
	//TODO:: something here isn't getting the appropriate error message; gives null/blank string
	protected String getErrorMessage(E e) {
		StringBuilder errorMessages = new StringBuilder();
		
		if (e instanceof UnauthorizedException) {
			errorMessages.append("User not logged in; did not receive token. ");
		}
		
		Throwable lastE = null;
		Throwable curE = e;
		while (curE != null && lastE != curE) {
			if (e.getMessage() != null) {
				errorMessages.append(e.getMessage());
			}
			lastE = curE;
			curE = e.getCause();
		}
		return errorMessages.toString();
	}
	
	@Override
	public Response toResponse(E e) {
		String errorMessage = this.getErrorMessage(e);
		
		log.warn("User not authorized to access: {} - {} Message(s): {}", this.getUriInfo().getRequestUri(), e.getClass().getName(), errorMessage);
		
		//        log.info("Cookie: {}", authCookies);
		URI uri = this.getUriInfo().getRequestUri();
		if (isAtUiEndpoint()) {
			return Response.seeOther( //seeOther = 303 redirect
									  UriBuilder.fromUri("/")
												.queryParam("messageHeading", "Unauthorized")
												.queryParam("message",
															"Please login to access this page. " + (errorMessage != null && !errorMessage.isBlank() ?
															"Error: " + errorMessage : ""))
												.queryParam("messageType", "danger")
												.queryParam(
													"returnPath",
													uri.getPath() + (uri.getQuery() == null || uri.getQuery().isBlank() ? "" :
																												  "?" + uri.getQuery())
												)
												.build()
						   )//build the URL where you want to redirect
						   //                    .entity("Not authorized")//entity is not required
						   .cookie(
							   UiUtils.getAuthRemovalCookie()
						   )
						   .build();
		}
		log.info("Erring exception for url that wasn't in ui.");
		return Response.status(Response.Status.UNAUTHORIZED).entity(errorMessage).build();
	}
}
