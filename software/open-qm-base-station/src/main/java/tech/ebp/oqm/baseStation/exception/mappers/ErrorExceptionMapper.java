package tech.ebp.oqm.baseStation.exception.mappers;

import lombok.SneakyThrows;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

//not worth doing this?
@Provider
public class ErrorExceptionMapper extends AwareExceptionMapper<Throwable> {
	
	@SneakyThrows
	@Override
	public Response toResponse(Throwable exception) {
		assertNotAtUiEndpoint(exception);
		
		return Response.serverError()
					   .entity(
						   new ErrorMessage(
							   "An unexpected error occurred, please consider contacting the developers about this issue. Error: " + exception.getMessage(),
							   exception
						   )
					   )
				   .build();
	}
}
