package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "externalItemSearch")
public interface ExternalItemSearchClient {

	@GET
	@Path("/providers")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> allProviderInfo();

	@GET
	@Path("barcode/{barcode}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> searchBarcode(@PathParam("barcode") String barcode);

	@GET
	@Path("webpage-scrape/{webpage}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> scanWebpage(@PathParam("webpage") String page);

	@GET
	@Path("lego/part/{partNo}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> searchLegoPart(@PathParam("partNo") String partNo);
}
