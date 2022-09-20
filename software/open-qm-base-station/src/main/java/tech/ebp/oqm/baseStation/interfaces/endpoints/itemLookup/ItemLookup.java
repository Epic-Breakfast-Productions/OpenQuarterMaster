package tech.ebp.oqm.baseStation.interfaces.endpoints.itemLookup;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.productLookup.ProductLookupService;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResults;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


/**
 * TODO:: reorganize these endpoints
 */
@Traced
@Slf4j
@Path("/api/externalItemLookup")
@Tags({@Tag(name = "External Item Lookup", description = "Endpoints for searching for items from other places.")})
@RequestScoped
public class ItemLookup extends EndpointProvider {
	
	@Inject
	JsonWebToken jwt;
	
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
	public Response providerInfo(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.jwt, securityContext);
		
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
	public Response enabledProviderInfo(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.jwt, securityContext);
		
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
		@Context SecurityContext securityContext,
		@PathParam("barcode") String barcode
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.searchBarcode(barcode)).build();
	}
	
	@GET
	@Path("product/webpage/{webpage}")
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
		@Context SecurityContext securityContext,
		@PathParam("webpage") String page
	) throws MalformedURLException, ExecutionException, InterruptedException {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.scanPage(new URL(page))).build();
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
		@Context SecurityContext securityContext,
		@PathParam("partNo") String partNo
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.searchLegoPart(partNo)).build();
	}
}
