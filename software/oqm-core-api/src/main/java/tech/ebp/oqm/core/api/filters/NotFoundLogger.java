package tech.ebp.oqm.core.api.filters;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class NotFoundLogger implements ExceptionMapper<NotFoundException> {

	@Context
	UriInfo uriInfo;

	@Context
	HttpServerRequest request;


	@Override
	public Response toResponse(NotFoundException exception) {
		log.info(
			"RESOURCE NOT FOUND Request from {} - {}:{}  ssl?: {} / {}",
			request.remoteAddress().toString(),
			request.method(),
			uriInfo.getPath(),
			request.isSSL(),
			exception.getMessage()
		);
		log.debug(
			"RESOURCE NOT FOUND Request headers: ({}): {}",
			request.headers().size(),
			request.headers()
		);
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}