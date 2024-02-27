package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/media/image")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ImagePassthrough extends PassthroughProvider {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@GET
	@Path("/for/{type}/{id}")
	public Uni<Response> imageForObj(@PathParam("type") String type,  @PathParam("id") String objId){
		return this.oqmCoreApiClient.imageForObject(this.getBearerHeaderStr(), type, objId)
				   .map( output ->
							 Response.ok(output).build()
				   );
	}
	
}
