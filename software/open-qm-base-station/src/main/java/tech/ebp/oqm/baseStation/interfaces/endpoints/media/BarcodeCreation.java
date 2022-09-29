package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.barcode.BarcodeService;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/api/media/code")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@ApplicationScoped
public class BarcodeCreation extends EndpointProvider {
	
	@Inject
	BarcodeService barcodeService;
	
	@Inject
	JsonWebToken jwt;
	
	@GET
	@Path("barcode/{code}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBarcode(
		@Context SecurityContext ctx,
		@PathParam("code") String data
	) {
		log.info("Getting currency of server.");
		return Response.status(Response.Status.OK)
					   .entity(this.barcodeService.getBarcodeData(data))
					   .header("Content-Disposition", "attachment;filename=" + "barcode.svg")
					   .type(BarcodeService.DATA_MEDIA_TYPE)
					   .build();
	}
	
	@GET
	@Path("qrcode/{code}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQrCode(
		@Context SecurityContext ctx,
		@PathParam("code") String data
	) {
		log.info("Getting currency of server.");
		return Response.status(Response.Status.OK)
					   .entity(this.barcodeService.getQrCodeData(data))
					   .header("Content-Disposition", "attachment;filename=" + "barcode.svg")
					   .type(BarcodeService.DATA_MEDIA_TYPE)
					   .build();
	}
}
