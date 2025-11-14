package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.plugins.externalItemSearch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.core.baseStation.service.ExternalItemSearchClient;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_PLUGIN_ROOT + "/itemLookup")
@Tags({@Tag(name = "External Item Lookup", description = "Endpoints for searching for items from other places.")})
@Authenticated
@RequestScoped
public class ItemLookupRestInterface extends PassthroughProvider {
	
	@RestClient
	ExternalItemSearchClient externalItemSearchClient;
	
	@GET
	@Path("/providers")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> allProviderInfo() {
		return this.handleCall(
			this.externalItemSearchClient.allProviderInfo()
		);
	}
	
	@GET
	@Path("barcode/{barcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> searchBarcode(
		@PathParam("barcode") String barcode
	) {
		return this.handleCall(
			this.externalItemSearchClient.searchBarcode(barcode)
		);
	}
	
	@GET
	@Path("webpage-scrape/{webpage}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> scanWebpage(
		@PathParam("webpage") String page
	) {
		return this.handleCall(
			this.externalItemSearchClient.scanWebpage(page)
		);
	}
	
	@GET
	@Path("lego/part/{partNo}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> searchLegoPart(
		@PathParam("partNo") String partNo
	) {
		return this.handleCall(
			this.externalItemSearchClient.searchLegoPart(partNo)
		);
	}
}