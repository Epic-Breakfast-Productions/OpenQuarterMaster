package tech.ebp.oqm.core.baseStation.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.model.CodeImageType;
import tech.ebp.oqm.core.baseStation.model.ObjectCodeContentType;
import tech.ebp.oqm.core.baseStation.service.BarcodeService;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

/**
 * TODO:: review produces
 */
@Slf4j
@Path("/api/media/code")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class BarcodeImageCreation extends ApiProvider {
	
	@Inject
	BarcodeService barcodeService;
	@RestClient
	OqmCoreApiClientService coreApiClientService;
	
	@ConfigProperty(name = "runningInfo.baseUrl")
	String selfBaseUrl;
	@ConfigProperty(name = "oqm.core.api.baseUri")
	String apiUri;
	
	private Response getBarcodeResponse(
		CodeImageType codeType,
		String data,
		String label,
		ObjectCodeContentType codeContent
	) {
		return Response.status(Response.Status.OK)
				   .entity(this.barcodeService.getCodeData(codeType, data))
				   .header("Content-Disposition", "attachment;filename=" + label.replaceAll("\\W+", "") + "_" + codeContent + "_" + codeType + ".svg")
				   .type(BarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
	
	@GET
	@Path("{codeType}/{code}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBarcode(
		@PathParam("codeType") CodeImageType type,
		@PathParam("code") String data
	) {
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQrCode(
		@NonNull @PathParam("object") String object,
		@NonNull @PathParam("id") String id,
		@NonNull @PathParam("codeType") CodeImageType codeType,
		@NonNull @PathParam("codeContentType") ObjectCodeContentType codeContent
	) {
		log.info("Getting {} with {} for object {} {}.", codeType, codeContent, object, id);
		
		//ensure object exists
		String label = null;
		ObjectNode item = null;
		switch (object) {
			case "storageBlock":
				label = this.coreApiClientService.storageBlockGet(this.getBearerHeaderStr(), this.getSelectedDb(), id).await().indefinitely().get("labelText").asText();
				break;
			case "item":
				item = this.coreApiClientService.invItemGet(this.getBearerHeaderStr(), this.getSelectedDb(), id).await().indefinitely();
				label = item.get("name").asText();
				break;
			default:
				throw new IllegalArgumentException("Invalid object to get a code for: " + object);
		}
		
		String data = null;
		switch (object) {
			case "storageBlock":
				switch (codeContent) {
					case id:
					case barcode:
						data = id;
						break;
					case apilink:
						data = this.apiUri + "/api/v1/inventory/storage-block/" + id;
						break;
					case uilink:
						data = this.selfBaseUrl + "/storage?view=" + id;
						break;
				}
				break;
			case "item":
				switch (codeContent) {
					case id:
						data = id;
						break;
					case apilink:
						data = this.apiUri + "/api/v1/inventory/item/" + id;
						break;
					case uilink:
						data = this.selfBaseUrl + "/items?view=" + id;
						break;
					case barcode:
						data = item.get("barcode").asText();
				}
				break;
		}
		
		if (label == null || label.isBlank()) {
			throw new IllegalStateException("Should not happen.");
		}
		if (data == null || data.isBlank()) {
			throw new IllegalStateException("Should not happen.");
		}
		
		return this.getBarcodeResponse(
			codeType,
			data,
			label,
			codeContent
		);
	}
	
	@GET
	@Path("item/{id}/barcode")
	@Operation(
		summary = "Gets a bar or QR code related to the object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItemBarcode(
		@NonNull @PathParam("id") String id
	) {
		ObjectNode item = this.coreApiClientService.invItemGet(this.getBearerHeaderStr(), this.getSelectedDb(), id).await().indefinitely();
		String barcode = item.get("barcode").asText();
		
		if (barcode == null || barcode.isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Barcode field for item " + id + " (" + item.get("name").asText() + " is empty.").build();
		}
		
		return this.getBarcodeResponse(
			CodeImageType.barcode,
			barcode,
			item.get("name").asText(),
			ObjectCodeContentType.barcode
		);
	}
	
	//TODO:: think about these next two
	
//	@GET
//	@Path("item/{id}/barcode/stored/{storageBlockId}")
//	@Operation(
//		summary = "Gets a bar or QR code related to the object."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Got the currency."
//	)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getStoredItemBarcodeAmountSimple(
//		@NonNull @PathParam("id") String itemId,
//		@NonNull @PathParam("storageBlockId") String storageBlockId
//	) {
//		ObjectNode item = this.coreApiClientService.invItemGet(this.getBearerHeaderStr(), id).await().indefinitely();
//
//		SingleAmountStoredWrapper wrapper;
//
//		try {
//			wrapper = (SingleAmountStoredWrapper) item.getStoredWrapperForStorage(new ObjectId(storageBlockId), false);
//		} catch(ClassCastException e) {
//			return Response.status(Response.Status.BAD_REQUEST).entity("Item given not a simple amount stored.").build();
//		}
//
//		if (wrapper == null) {
//			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not store anything at that storage location.").build();
//		}
//
//		String barcode = wrapper.getStored().getBarcode();
//
//		if (barcode == null || barcode.isBlank()) {
//			return Response.status(Response.Status.BAD_REQUEST)
//					   .entity("Barcode field for stored item " + itemId + " (" + item.getName() + ") in block " + storageBlockId + " is empty.")
//					   .build();
//		}
//
//		return Response.status(Response.Status.OK)
//				   .entity(this.barcodeService.getCodeData(CodeImageType.barcode, barcode))
//				   .header("Content-Disposition", "attachment;filename=" + item.getName().replaceAll("\\W+", "") + "_" + storageBlockId + "_barcode.svg")
//				   .type(BarcodeService.DATA_MEDIA_TYPE)
//				   .build();
//	}
	
//	@GET
//	@Path("item/{id}/barcode/stored/{storageBlockId}/{index}")
//	@Operation(
//		summary = "Gets a bar or QR code related to the object."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Got the currency."
//	)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getStoredItemBarcodeAmountListTracked(
//		@NonNull @PathParam("id") String itemId,
//		@NonNull @PathParam("storageBlockId") String storageBlockId,
//		@NonNull @PathParam("index") String index
//	) {
//		InventoryItem<?, ?, ?> item = this.inventoryItemService.get(itemId);
//
//		String barcode;
//
//		StoredWrapper<?, ?> wrapper = item.getStoredWrapperForStorage(new ObjectId(storageBlockId), false);
//
//		if (wrapper == null) {
//			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not store anything at that storage location.").build();
//		}
//
//		if (wrapper instanceof TrackedMapStoredWrapper) {
//			TrackedStored stored = ((TrackedMapStoredWrapper) wrapper).getStored().get(index);
//
//			if (stored == null) {
//				return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not have a tracked item with that identifier.").build();
//			}
//			barcode = stored.getBarcode();
//		} else if (wrapper instanceof ListAmountStoredWrapper) {
//			AmountStored stored;
//
//			try {
//				stored = ((ListAmountStoredWrapper) wrapper).getStored().get(Integer.parseInt(index));
//			} catch(NumberFormatException e) {
//				return Response.status(Response.Status.BAD_REQUEST).entity("Item given stores items with a numerical index. Non-numerical index gotten.").build();
//			} catch(IndexOutOfBoundsException e) {
//				return Response.status(Response.Status.BAD_REQUEST).entity("Index out of bounds.").build();
//			}
//
//			barcode = stored.getBarcode();
//		} else {
//			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does store items with an index.").build();
//		}
//
//		if (barcode == null || barcode.isBlank()) {
//			return Response.status(Response.Status.BAD_REQUEST).entity("Barcode field for stored item " + itemId + " (" + item.getName() + ") in block " + storageBlockId + " at "
//																	   + "index " + index + " is empty.").build();
//		}
//
//		return Response.status(Response.Status.OK)
//				   .entity(this.barcodeService.getCodeData(CodeImageType.barcode, barcode))
//				   .header(
//					   "Content-Disposition",
//					   "attachment;filename=" + item.getName().replaceAll("\\W+", "") + "_" + storageBlockId + "_" + index.replaceAll("\\W+", "") + "_barcode.svg"
//				   )
//				   .type(BarcodeService.DATA_MEDIA_TYPE)
//				   .build();
//	}
}
