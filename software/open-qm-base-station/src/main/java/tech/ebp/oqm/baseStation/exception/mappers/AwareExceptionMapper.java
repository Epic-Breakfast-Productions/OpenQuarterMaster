package tech.ebp.oqm.baseStation.exception.mappers;

import lombok.Getter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class AwareExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
	
	protected boolean isAtUiEndpoint() {
		String path = uriInfo.getPath();
		
		return !path.startsWith("/api") &&
			   !path.startsWith("/q/") &&
			   !path.startsWith("/openapi");
	}
	
	protected void assertNotAtUiEndpoint(E exception) throws E {
		if(isAtUiEndpoint()){
			throw exception;
		}
	}
	
	@Getter
	@Context
	UriInfo uriInfo;
}
