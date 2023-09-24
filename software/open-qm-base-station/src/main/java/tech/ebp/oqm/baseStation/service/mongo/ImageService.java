package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;

import java.util.Map;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class ImageService extends MongoHistoriedObjectService<Image, ImageSearch> {
	
	private StorageBlockService storageBlockService;
	private ItemCategoryService itemCategoryService;
	private InventoryItemService inventoryItemService;
	
	ImageService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ImageService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		StorageBlockService storageBlockService,
		ItemCategoryService itemCategoryService,
		InventoryItemService inventoryItemService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			Image.class,
			false
		);
		this.storageBlockService = storageBlockService;
		this.itemCategoryService = itemCategoryService;
		this.inventoryItemService = inventoryItemService;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, Image newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(ClientSession cs, Image objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		refs = this.itemCategoryService.getItemCatsReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.itemCategoryService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
}
