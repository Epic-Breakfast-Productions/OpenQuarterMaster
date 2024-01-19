package tech.ebp.oqm.baseStation.interfaces.endpoints.itemLookup;

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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResults;
import tech.ebp.oqm.baseStation.service.productLookup.ProductLookupService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;


/**
 * TODO:: reorganize these endpoints
 */
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/externalItemLookup")
@Tags({@Tag(name = "External Item Lookup", description = "Endpoints for searching for items from other places.")})
@RequestScoped
public class ItemLookup extends EndpointProvider {
	
	@Inject
	ProductLookupService productLookupService;
	
	@GET
	@Path("/product/providers")
	@Operation(
		summary = "Gets information on supported product search providers."
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
	public Response providerInfo() {
		return Response.ok(this.productLookupService.getProductProviderInfo()).build();
	}
	
	@GET
	@Path("/product/providers/enabled")
	@Operation(
		summary = "Gets information on supported and enabled product search providers."
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
	public Response enabledProviderInfo() {
		return Response.ok(
			this.productLookupService.getProductProviderInfo().stream().filter(ExtItemLookupProviderInfo::isEnabled)
		).build();
	}
	
	@GET
	@Path("product/barcode/{barcode}")
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
	@Path("webpage/scrape/{webpage}")
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
	@Path("/webpage/providers")
	@Operation(
		summary = "Gets information on supported web scraping providers."
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
	public Response webScrapeProviderInfo() {
		return Response.ok(this.productLookupService.getSupportedPageScanInfo()).build();
	}
	
	@GET
	@Path("/webpage/providers/enabled")
	@Operation(
		summary = "Gets information on supported and enabled web scraping providers."
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
	public Response enablesWebScrapeProviderInfo() {
		return Response.ok(
			this.productLookupService.getSupportedPageScanInfo().stream().filter(ExtItemLookupProviderInfo::isEnabled)
		).build();
	}
	
	@GET
	@Path("lego/part/{partNo}")
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
	public Response searchLegoPart(
		@PathParam("partNo") String partNo
	) {
		return Response.ok(this.productLookupService.searchLegoPart(partNo)).build();
	}
	
	@GET
	@Path("/lego/providers")
	@Operation(
		summary = "Gets information on supported lego search providers."
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
	public Response legoProviderInfo() {
		return Response.ok(this.productLookupService.getLegoProviderInfo()).build();
	}
	
	@GET
	@Path("/lego/providers/enabled")
	@Operation(
		summary = "Gets information on supported and enabled lego search providers."
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
	public Response enabledLegoProviderInfo() {
		return Response.ok(
			this.productLookupService.getLegoProviderInfo().stream().filter(ExtItemLookupProviderInfo::isEnabled)
		).build();
	}
}
