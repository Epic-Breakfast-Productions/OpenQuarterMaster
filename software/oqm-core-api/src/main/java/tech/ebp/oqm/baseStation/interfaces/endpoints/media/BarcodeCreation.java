package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.media.CodeImageType;
import tech.ebp.oqm.baseStation.model.rest.media.ObjectCodeContentType;
import tech.ebp.oqm.baseStation.model.rest.storage.IMAGED_OBJ_TYPE_NAME;
import tech.ebp.oqm.baseStation.service.barcode.BarcodeService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

/**
 * TODO:: review produces
 */
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/code")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@ApplicationScoped
public class BarcodeCreation extends EndpointProvider {
	
	@Inject
	BarcodeService barcodeService;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	InventoryItemService inventoryItemService;
	
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
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQrCode(
		@NonNull @PathParam("object") IMAGED_OBJ_TYPE_NAME object,
		@NonNull @PathParam("id") String id,
		@NonNull @PathParam("codeType") CodeImageType codeType,
		@NonNull @PathParam("codeContentType") ObjectCodeContentType codeContent
	) {
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
						data = this.selfBaseUrl + EndpointProvider.ROOT_API_ENDPOINT_V1 + "/inventory/storage-block/" + id;
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
						data = this.selfBaseUrl + EndpointProvider.ROOT_API_ENDPOINT_V1 + "/inventory/item/" + id;
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
					   .header("Content-Disposition", "attachment;filename=" + label.replaceAll("\\W+", "") + "_"+codeContent+"_"+codeType+".svg")
					   .type(BarcodeService.DATA_MEDIA_TYPE)
					   .build();
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
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItemBarcode(
		@NonNull @PathParam("id") String id
	) {
		InventoryItem<?, ?, ?> item = this.inventoryItemService.get(id);
		String barcode = item.getBarcode();
		
		if(barcode == null || barcode.isBlank()){
			return Response.status(Response.Status.BAD_REQUEST).entity("Barcode field for item " + id + " (" + item.getName() + ") is empty.").build();
		}
		
		return Response.status(Response.Status.OK)
				   .entity(this.barcodeService.getCodeData(CodeImageType.barcode, barcode))
				   .header("Content-Disposition", "attachment;filename=" + item.getName().replaceAll("\\W+", "") + "_barcode.svg")
				   .type(BarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
	
	@GET
	@Path("item/{id}/barcode/stored/{storageBlockId}")
	@Operation(
		summary = "Gets a bar or QR code related to the object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoredItemBarcodeAmountSimple(
		@NonNull @PathParam("id") String itemId,
		@NonNull @PathParam("storageBlockId") String storageBlockId
	) {
		InventoryItem<?, ?, ?> item = this.inventoryItemService.get(itemId);
		
		SingleAmountStoredWrapper wrapper;
		
		try {
			wrapper = (SingleAmountStoredWrapper) item.getStoredWrapperForStorage(new ObjectId(storageBlockId), false);
		} catch(ClassCastException e){
			return Response.status(Response.Status.BAD_REQUEST).entity("Item given not a simple amount stored.").build();
		}
		
		if(wrapper == null){
			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not store anything at that storage location.").build();
		}
		
		String barcode = wrapper.getStored().getBarcode();
		
		if(barcode == null || barcode.isBlank()){
			return Response.status(Response.Status.BAD_REQUEST).entity("Barcode field for stored item " + itemId + " (" + item.getName() + ") in block "+storageBlockId+" is empty.").build();
		}
		
		return Response.status(Response.Status.OK)
				   .entity(this.barcodeService.getCodeData(CodeImageType.barcode, barcode))
				   .header("Content-Disposition", "attachment;filename=" + item.getName().replaceAll("\\W+", "") + "_"+storageBlockId+"_barcode.svg")
				   .type(BarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
	
	@GET
	@Path("item/{id}/barcode/stored/{storageBlockId}/{index}")
	@Operation(
		summary = "Gets a bar or QR code related to the object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoredItemBarcodeAmountListTracked(
		@NonNull @PathParam("id") String itemId,
		@NonNull @PathParam("storageBlockId") String storageBlockId,
		@NonNull @PathParam("index") String index
	) {
		InventoryItem<?, ?, ?> item = this.inventoryItemService.get(itemId);
		
		String barcode;
		
		StoredWrapper<?,?> wrapper = item.getStoredWrapperForStorage(new ObjectId(storageBlockId), false);
		
		if(wrapper == null){
			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not store anything at that storage location.").build();
		}
		
		if(wrapper instanceof TrackedMapStoredWrapper){
			TrackedStored stored = ((TrackedMapStoredWrapper) wrapper).getStored().get(index);
			
			if(stored == null){
				return Response.status(Response.Status.BAD_REQUEST).entity("Item given does not have a tracked item with that identifier.").build();
			}
			barcode = stored.getBarcode();
		} else if(wrapper instanceof ListAmountStoredWrapper) {
			AmountStored stored;
			
			try{
				stored = ((ListAmountStoredWrapper)wrapper).getStored().get(Integer.parseInt(index));
			} catch(NumberFormatException e){
				return Response.status(Response.Status.BAD_REQUEST).entity("Item given stores items with a numerical index. Non-numerical index gotten.").build();
			} catch(IndexOutOfBoundsException e){
				return Response.status(Response.Status.BAD_REQUEST).entity("Index out of bounds.").build();
			}
			
			barcode = stored.getBarcode();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity("Item given does store items with an index.").build();
		}
		
		if(barcode == null || barcode.isBlank()){
			return Response.status(Response.Status.BAD_REQUEST).entity("Barcode field for stored item " + itemId + " (" + item.getName() + ") in block "+storageBlockId+" at "
																	   + "index "+index+" is empty.").build();
		}
		
		return Response.status(Response.Status.OK)
				   .entity(this.barcodeService.getCodeData(CodeImageType.barcode, barcode))
				   .header("Content-Disposition", "attachment;filename=" + item.getName().replaceAll("\\W+", "") + "_"+storageBlockId+"_"+index.replaceAll("\\W+", "")+"_barcode.svg")
				   .type(BarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
}