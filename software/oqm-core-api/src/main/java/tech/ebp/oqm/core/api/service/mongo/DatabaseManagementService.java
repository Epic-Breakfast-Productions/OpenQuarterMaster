package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Getter(AccessLevel.PRIVATE)
public class DatabaseManagementService {
	
	FileAttachmentService fileAttachmentService;
	ImageService imageService;
	InteractingEntityService interactingEntityService;
	ItemCategoryService itemCategoryService;
	ItemCheckoutService itemCheckoutService;
	ItemListService itemListService;
	StorageBlockService storageBlockService;
	InventoryItemService inventoryItemService;
	
	List<MongoDbAwareService<?,?,?>> removeList = new ArrayList<>();
	
	@Inject
	public DatabaseManagementService(
		FileAttachmentService fileAttachmentService,
		ImageService imageService,
		InteractingEntityService interactingEntityService,
		ItemCategoryService itemCategoryService,
		ItemCheckoutService itemCheckoutService,
		ItemListService itemListService,
		StorageBlockService storageBlockService,
		InventoryItemService inventoryItemService
	) {
		this.itemListService = itemListService;
		this.itemCheckoutService = itemCheckoutService;
		this.inventoryItemService = inventoryItemService;
		this.storageBlockService = storageBlockService;
		this.itemCategoryService = itemCategoryService;
		this.fileAttachmentService = fileAttachmentService;
		this.imageService = imageService;
		this.interactingEntityService = interactingEntityService;
		
		this.removeList.add(this.getItemListService());
		this.removeList.add(this.getItemCheckoutService());
		this.removeList.add(this.getInventoryItemService());
		this.removeList.add(this.getStorageBlockService());
		this.removeList.add(this.getItemCategoryService());
		this.removeList.add(this.getFileAttachmentService());
		this.removeList.add(this.getImageService());
	}
	
	/**
	 * @param oqmDbIdOrName
	 * @param performingEntity
	 * @return
	 */
	public Map<String, Long> clearDb(String oqmDbIdOrName, InteractingEntity performingEntity){
		Map<String, Long> output = new HashMap<>();
		try(
			ClientSession session = this.fileAttachmentService.getNewClientSession(true)
		){
			for(MongoDbAwareService<?,?,?> curService : this.getRemoveList()){
				
				output.put(curService.getCollectionName(), curService.clear(oqmDbIdOrName, session));
			}
			
			//TODO:: add to admin action log (need to make that)
			session.commitTransaction();
		}
		return output;
	}
}
