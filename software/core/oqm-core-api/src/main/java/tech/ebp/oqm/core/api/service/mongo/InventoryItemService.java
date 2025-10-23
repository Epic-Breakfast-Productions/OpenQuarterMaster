package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.InvItemCollectionStats;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.ToGenerate;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.GeneratedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.StoredInBlockStats;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.service.ItemStatsService;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.units.indriya.quantity.Quantities;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

/**
 * TODO::
 *    - Figure out how to handle expired state when adding, updating
 */
@Named("InventoryItemService")
@Slf4j
@ApplicationScoped
public class InventoryItemService extends MongoHistoriedObjectService<InventoryItem, InventoryItemSearch, InvItemCollectionStats> {
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemCheckoutService itemCheckoutService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	StorageBlockService storageBlockService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemCategoryService itemCategoryService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	StoredService storedService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	IdentifierGenerationService identifierGenerationService;
	
	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;
	
	@Getter(AccessLevel.PRIVATE)
	@Inject
	ItemStatsService itemStatsService;
	
	public InventoryItemService() {
		super(InventoryItem.class, false);
		try (InstanceHandle<HistoryEventNotificationService> container = Arc.container().instance(HistoryEventNotificationService.class)) {
			this.hens = container.get();
		}
	}
	
	//TODO:: this better
	@Override
	public Set<String> getDisallowedUpdateFields() {
		Set<String> output = new HashSet<>(super.getDisallowedUpdateFields());
		output.add("storageType");
		output.add("stats");
		return output;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, InventoryItem newOrChangedObject, ClientSession clientSession) throws ValidationException {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
		
		for (ObjectId curCategoryId : newOrChangedObject.getCategories()) {
			try {
				this.getItemCategoryService().get(oqmDbIdOrName, curCategoryId);
			} catch(DbNotFoundException e) {
				throw new ValidationException("Item category " + curCategoryId.toHexString() + " does not exist.", e);
			}
		}
		
		for (ObjectId curObjectId : newOrChangedObject.getStorageBlocks()) {
			try {
				this.getStorageBlockService().get(oqmDbIdOrName, curObjectId);
			} catch(DbNotFoundException e) {
				throw new ValidationException("Storage block " + curObjectId.toHexString() + " does not exist.", e);
			}
		}
		
		List<InventoryItem> nameResults = this.list(oqmDbIdOrName, eq("name", newOrChangedObject.getName()), null, null);
		if (!nameResults.isEmpty()) {
			if (newObject) {
				throw new ValidationException("Item with name '" + newOrChangedObject.getName() + "' already exists.");
			} else {
				for (InventoryItem curMatcingName : nameResults) {
					if (!curMatcingName.getId().equals(newOrChangedObject.getId())) {
						throw new ValidationException("Item with name '" + newOrChangedObject.getName() + "' already exists.");
					}
				}
			}
		}
		
		for (UniqueId curUniqueId : newOrChangedObject.getUniqueIds()) {
			if (curUniqueId.getType() == UniqueIdType.TO_GENERATE) {
				continue;
			}
			
			List<InventoryItem> uniqueIdresults = this.getItemsWithUniqueId(oqmDbIdOrName, clientSession, curUniqueId);
			if (!uniqueIdresults.isEmpty()) {
				if (newObject) {
					throw new ValidationException("Item with unique id '" + curUniqueId + "' already exists.");
				} else {
					for (InventoryItem curMatcingName : uniqueIdresults) {
						if (!curMatcingName.getId().equals(newOrChangedObject.getId())) {
							throw new ValidationException("Item with unique id '" + curUniqueId + "' already exists.");
						}
					}
				}
			}
		}
		
		if (!newObject) {
			//TODO:: in try?
			InventoryItem existing = this.get(oqmDbIdOrName, newOrChangedObject.getId());
			
			if (!existing.getUnit().isCompatible(newOrChangedObject.getUnit())) {
				throw new ValidationException("New unit not compatible with current unit.");
			}
		} else {
			//if new item, and stats are null, set new stats. No stored should exist so this should be representative enough to start. Maybe generate stats?
			if (newOrChangedObject.getStats() == null) {
				newOrChangedObject.setStats(
					new ItemStoredStats(newOrChangedObject.getUnit())
				);
			}
		}
	}
	
	@Override
	public void massageIncomingData(String oqmDbIdOrName, @NonNull InventoryItem item) {
		super.massageIncomingData(oqmDbIdOrName, item);
		
		item.setGeneralIds(this.getIdentifierGenerationService().generateIdPlaceholders(oqmDbIdOrName, item.getGeneralIds()));
		item.setUniqueIds(this.getIdentifierGenerationService().generateIdPlaceholders(oqmDbIdOrName, item.getUniqueIds()));
		
	}
	
	@Override
	public InvItemCollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, InvItemCollectionStats.builder())
				   .numExpired(this.getNumStoredExpired(oqmDbIdOrName))
				   .numExpireWarn(this.getNumStoredExpiryWarn(oqmDbIdOrName))
				   .numLowStock(this.getNumLowStock(oqmDbIdOrName))
				   .build();
	}
	
	@Override
	@WithSpan
	public ObjectId add(String oqmDbIdOrName, ClientSession session, @NonNull @Valid InventoryItem item, InteractingEntity entity, HistoryDetail... details) {
		item.setStats( //Build simple stats on the premise of not having any stored items
			ItemStoredStats.builder()
				.total((Quantities.getQuantity(0, item.getUnit())))
				.lowStock(
					item.getLowStockThreshold() != null && item.getLowStockThreshold().getValue().longValue() != 0
				)
				.storageBlockStats(
					item.getStorageBlocks().stream()
						.collect(Collectors.toMap(
							Function.identity(),
							(storageBlockId)->StoredInBlockStats.builder().build()
						))
				)
				.build()
		);
		
		return super.add(oqmDbIdOrName, session, item, entity, details);
	}
	
	@Override
	@WithSpan
	public InventoryItem update(String oqmDbIdOrName, ClientSession cs, InventoryItem object, InteractingEntity entity, HistoryDetail... details) throws DbNotFoundException {
		InventoryItem output = super.update(oqmDbIdOrName, cs, object, entity, details);
		
		//TODO:: update again if necessary #929
		this.getItemStatsService().postItemUpdateProcess(oqmDbIdOrName, cs, object, entity, details);
		
		return output;
	}
	
	
	@Override
	public InventoryItem remove(String oqmDbIdOrName, ClientSession session, ObjectId objectId, InteractingEntity entity, HistoryDetail... details) {
		//TODO:: delete stored
		return super.remove(oqmDbIdOrName, session, objectId, entity, details);
	}
	
	@WithSpan
	public List<InventoryItem> getItemsInBlock(String oqmDbIdOrName, ObjectId storageBlockId) {
		return this.list(
			oqmDbIdOrName,
			exists("storageMap." + storageBlockId.toHexString()),
			null,
			null
		);
	}
	
	@WithSpan
	public List<InventoryItem> getItemsInBlock(String oqmDbIdOrName, String storageBlockId) {
		return this.getItemsInBlock(oqmDbIdOrName, new ObjectId(storageBlockId));
	}
	
	@WithSpan
	public long getNumStoredExpired(String oqmDbIdOrName) {
		return this.getSumOfIntField(oqmDbIdOrName, "numExpired");
	}
	
	@WithSpan
	public long getNumStoredExpiryWarn(String oqmDbIdOrName) {
		return this.getSumOfIntField(oqmDbIdOrName, "numExpiryWarn");
	}
	
	@WithSpan
	public long getNumLowStock(String oqmDbIdOrName) {
		return this.getSumOfIntField(oqmDbIdOrName, "numLowStock");
	}
	
	public List<InventoryItem> getItemsWithUniqueId(String oqmDbIdOrName, ClientSession clientSession, UniqueId id) {
		
		Bson filter;
		
		switch (id.getType()) {
			case GENERATED -> {
				filter = and(
					eq("uniqueIds.generatedFrom", ((GeneratedUniqueId) id).getGeneratedFrom()),
					eq("uniqueIds.value", id.getValue())
				);
			}
			case PROVIDED -> {
				filter = and(
					eq("uniqueIds.value", id.getValue())
				);
			}
			default -> {
				return Collections.emptyList();
			}
		}
		
		List<InventoryItem> list = new ArrayList<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			filter,
			null,
			null
		).into(list);
		
		return list;
	}
	
	public Set<ObjectId> getItemsReferencing(String oqmDbIdOrName, ClientSession clientSession, Image image) {
		// { "imageIds": {$elemMatch: {$eq:ObjectId('6335f3c338a79a4377aea064')}} }
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("imageIds", image.getId()),
			null,
			null
		).map(InventoryItem::getId).into(list);
		
		return list;
	}
	
	public Set<ObjectId> getItemsReferencing(String oqmDbIdOrName, ClientSession clientSession, StorageBlock storageBlock) {
		Set<ObjectId> list = new TreeSet<>();
		
		//TODO:: figure out how find with query
		this.listIterator(oqmDbIdOrName, clientSession).forEach((InventoryItem item)->{
			if (item.getStorageBlocks().contains(storageBlock.getId())) {
				list.add(item.getId());
			}
		});
		
		return list;
	}
	
	public Set<ObjectId> getItemsReferencing(String oqmDbIdOrName, ClientSession clientSession, ItemCategory itemCategory) {
		// { "imageIds": {$elemMatch: {$eq:ObjectId('6335f3c338a79a4377aea064')}} }
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("categories", itemCategory.getId()),
			null,
			null
		).map(InventoryItem::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getItemsReferencing(String oqmDbIdOrName, ClientSession clientSession, FileAttachment fileAttachment) {
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("attachedFiles", fileAttachment.getId()),
			null,
			null
		).map(InventoryItem::getId).into(list);
		return list;
	}
	
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, InventoryItem item) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, item);
		
		Set<ObjectId> refs = this.itemCheckoutService.getItemCheckoutsReferencing(oqmDbIdOrName, cs, item);
		if (!refs.isEmpty()) {
			objsWithRefs.put(this.itemCheckoutService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return InventoryItem.CUR_SCHEMA_VERSION;
	}
	
	@Override
	public void runPostUpgrade(String oqmDbIdOrName, ClientSession cs, CollectionUpgradeResult upgradeResult) {
		super.runPostUpgrade(oqmDbIdOrName, cs, upgradeResult);
		
		//		log.info("client session: {}", cs);
		//		log.info("is ack: {}", this.getMongoClient().getWriteConcern().isAcknowledged());
		////		this.getDocumentCollection(oqmDbIdOrName).getWriteConcern()
		//
		//		this.getDocumentCollection(oqmDbIdOrName).find(cs).forEach((Document doc)->{
		//			log.info("Inv Item: {}", doc.toJson());
		//		});
		
		FindIterable<InventoryItem> it = this.listIterator(oqmDbIdOrName, cs);
		for (InventoryItem item : it) {
			try {
				item.setStats(this.itemStatsService.getItemStats(oqmDbIdOrName, cs, item.getId()));
				this.update(oqmDbIdOrName, cs, item, this.getCoreApiInteractingEntity());
			} catch(Exception e) {
				log.error("Error running post upgrade for inventory item: {}", item, e);
				throw e;
			}
		}
	}
}
