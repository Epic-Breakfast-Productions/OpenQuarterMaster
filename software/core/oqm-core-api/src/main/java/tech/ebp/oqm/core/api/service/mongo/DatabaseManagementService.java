package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.management.DbClearResult;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
@Getter(AccessLevel.PRIVATE)
public class DatabaseManagementService {

	OqmDatabaseService databaseService;
	
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
		OqmDatabaseService databaseService,
		FileAttachmentService fileAttachmentService,
		ImageService imageService,
		InteractingEntityService interactingEntityService,
		ItemCategoryService itemCategoryService,
		ItemCheckoutService itemCheckoutService,
		ItemListService itemListService,
		StorageBlockService storageBlockService,
		InventoryItemService inventoryItemService
	) {
		this.databaseService = databaseService;
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
	public DbClearResult clearDb(ClientSession cs, String oqmDbIdOrName, InteractingEntity performingEntity){
		DbCacheEntry db = this.getDatabaseService().getOqmDatabase(oqmDbIdOrName);
		List<CollectionClearResult> output = new ArrayList<>(this.getRemoveList().size());

		for(MongoDbAwareService<?,?,?> curService : this.getRemoveList()){
			output.add(curService.clear(oqmDbIdOrName, cs));
		}

		return DbClearResult.builder()
			.dbName(db.getDbName())
			.dbId(db.getDbId())
			.collectionClearResults(output)
			.build();
	}


	public List<DbClearResult> clearAllDbs(InteractingEntity performingEntity) throws Exception {
		log.info("Clearing ALL databases.");
		try (MongoSessionWrapper csw = new MongoSessionWrapper(null, this.getImageService())) {
			return csw.runTransaction(() -> {
				List<DbClearResult> output = new ArrayList<>(this.databaseService.getDatabases().size());
				for(DbCacheEntry cacheEntry : this.databaseService.getDatabases()){
					this.clearDb(
						csw.getClientSession(),
						cacheEntry.getDbId().toHexString(),
						performingEntity
					);
				}
				return output;
			});
		} catch (Exception e) {
			log.error("Failed to apply transaction: ", e);
			throw e;
		}
	}
}
