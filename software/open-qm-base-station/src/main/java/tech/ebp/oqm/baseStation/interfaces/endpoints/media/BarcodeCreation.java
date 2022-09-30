package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.barcode.BarcodeService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.media.CodeImageType;
import tech.ebp.oqm.lib.core.rest.media.ObjectCodeContentType;
import tech.ebp.oqm.lib.core.rest.storage.IMAGED_OBJ_TYPE_NAME;

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
	StorageBlockService storageBlockService;
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	JsonWebToken jwt;
	@ConfigProperty(name="runningInfo.baseUrl")
	String selfBaseUrl;
	
	@GET
	@Path("{codeType}/{code}")
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
		@PathParam("codeType") CodeImageType type,
		@PathParam("code") String data
	) {
		logRequestContext(this.jwt, ctx);
		log.info("Getting {}.", type);
		return Response.status(Response.Status.OK)
					   .entity(this.barcodeService.getCodeData(type, data))
					   .header("Content-Disposition", "attachment;filename=" + "code.svg")
					   .type(BarcodeService.DATA_MEDIA_TYPE)
					   .build();
	}
	
	@GET
	@Path("object/{object}/{id}/{codeType}/{codeContentType}")
	@Operation(
		summary = "Gets a bar or QR code related to the object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQrCode(
		@Context SecurityContext ctx,
		@NonNull @PathParam("object") IMAGED_OBJ_TYPE_NAME object,
		@NonNull @PathParam("id") String id,
		@NonNull @PathParam("codeType") CodeImageType codeType,
		@NonNull @PathParam("codeContentType") ObjectCodeContentType codeContent
	) {
		logRequestContext(this.jwt, ctx);
		log.info("Getting {} with {} for object {} {}.", codeType, codeContent, object, id);
		
		//ensure object exists
		String label = null;
		switch (object){
			case storageBlock:
				StorageBlock block = this.storageBlockService.get(id);
				label = block.getLabel() + (block.getNickname().isBlank()?"":"\n"+block.getNickname());
				break;
			case item:
				label = this.inventoryItemService.get(id).getName();
				break;
		}
		
		String data = null;
		switch (object){
			case storageBlock:
				switch (codeContent){
					case id:
						data = id;
						break;
					case apilink:
						data = this.selfBaseUrl + "/api/inventory/storage-block/" + id;
						break;
					case uilink:
						data = this.selfBaseUrl + "/storage?view=" + id;
						break;
				}
				break;
			case item:
				switch (codeContent){
					case id:
						data = id;
						break;
					case apilink:
						data = this.selfBaseUrl + "/api/inventory/item/" + id;
						break;
					case uilink:
						data = this.selfBaseUrl + "/items?view=" + id;
						break;
				}
				break;
		}
		
		if(label == null || label.isBlank()){
			throw new IllegalStateException("Should not happen.");
		}
		if(data == null || data.isBlank()){
			throw new IllegalStateException("Should not happen.");
		}
		
		return Response.status(Response.Status.OK)
					   .entity(this.barcodeService.getCodeData(codeType, data))
					   .header("Content-Disposition", "attachment;filename=" + label.replaceAll("\\W+", "") + ".svg")
					   .type(BarcodeService.DATA_MEDIA_TYPE)
					   .build();
	}
}
