package tech.ebp.oqm.plugin.extItemSearch.interfaces;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupResults;
import tech.ebp.oqm.plugin.extItemSearch.service.ExtItemLookupService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

@Slf4j
@Path("/api/v1")
@Tags({@Tag(name = "External Item Lookup", description = "Endpoints for searching for items from other places.")})
@RequestScoped
public class ItemLookupRestInterface {

	@Inject
	ExtItemLookupService productLookupService;

	@GET
	@Path("/providers")
	@Operation(
		summary = "Gets all supported providers."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = ExtItemLookupProviderInfo.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response allProviderInfo() {
		return Response.ok(this.productLookupService.getAllProviderInfo()).build();
	}

	@GET
	@Path("barcode/{barcode}")
	@Operation(
		summary = "Searches enabled providers for the barcode given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ExtItemLookupResults.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchBarcode(
		@PathParam("barcode") String barcode
	) {
		return Response.ok(this.productLookupService.searchBarcode(barcode)).build();
	}

	@GET
	@Path("webpage-scrape/{webpage}")
	@Operation(
		summary = "Scans the given webpage for product details."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ExtItemLookupResults.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response scanWebpage(
		@PathParam("webpage") String page
	) throws MalformedURLException, ExecutionException, InterruptedException {
		return Response.ok(this.productLookupService.scanPage(new URL(page))).build();
	}

	@GET
	@Path("lego/part/{partNo}")
	@Operation(
		summary = "Searches enabled providers for the lego part number."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ExtItemLookupResults.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchLegoPart(
		@PathParam("partNo") String partNo
	) {
		return Response.ok(this.productLookupService.searchLegoPart(partNo)).build();
	}

}