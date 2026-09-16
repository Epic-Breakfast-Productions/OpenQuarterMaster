package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.plugins.externalItemSearch;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.core.baseStation.service.ExternalItemSearchClient;

import java.util.List;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_PLUGIN_ROOT + "/itemLookup")
@Tags({@Tag(name = "External Item Lookup", description = "Endpoints for searching for items from other places.")})
@Authenticated
@RequestScoped
public class ItemLookupRestInterface extends PassthroughProvider {
	
	@RestClient
	ExternalItemSearchClient externalItemSearchClient;
	
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> search(@BeanParam ItemLookupRequest request) {
		return this.handleCall(
			this.externalItemSearchClient.search(request)
		);
	}
	
	public static class ItemLookupRequest {
		
		@Parameter(description = "The type of lookup to perform. If empty, will perform a text search.")
		@QueryParam("lookupMethod")
		@DefaultValue("TEXT")
		List<String> lookupMethods;
		
		@Parameter(description = "The data source(s) to use to search. If empty, any are used.")
		@QueryParam("service")
		List<String> services;
		
		@Parameter(
			description = "The source(s) to use to search. Distinct from 'services', as some sources can pull from many services. Example, one service might present data from Amazon,"
						  + " and other retailers. If empty, will search all available."
		)
		@QueryParam("source")
		List<String> sources;
		
		@NonNull
		@NotNull
		@NotBlank
		@QueryParam("q")
		String search;
		
		@QueryParam("keepNotFound")
		@DefaultValue("false")
		boolean keepNotFound;
	}
}