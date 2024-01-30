package tech.ebp.oqm.core.baseStation.interfaces.rest;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.OqmCoreApiClientService;

@Blocking
@Slf4j
@Path("/api/coreApiHealth")
@Tags({@Tag(name = "Passthrough")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ApiHealthCheckGet extends RestInterface {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@GET
	public Response getApiHealth(){
		return Response.ok(oqmCoreApiClient.getApiServerHealth()).build();
	}
	
}
