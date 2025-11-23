package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.GeneratedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.*;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.ItemAwareSearchResult;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static tech.ebp.oqm.core.api.model.object.storage.items.StorageType.*;

/**
 * TODO:: recalc stats on update if necessary #929
 */
@Named("StoredService")
@Slf4j
@ApplicationScoped
public class StoredService extends MongoHistoriedObjectService<Stored, StoredSearch, CollectionStats> {
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	InventoryItemService inventoryItemService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	IdentifierGenerationService identifierGenerationService;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemCheckoutService itemCheckoutService;
	
	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;
	
	
	@Override
	public Set<String> getDisallowedUpdateFields() {
		Set<String> output = new HashSet<>(super.getDisallowedUpdateFields());
		output.add("amount");
		output.add("item");
		output.add("storageBlock");
		output.add("type");
		return output;
	}
	
	public StoredService() {
		super(Stored.class, false);
		try (InstanceHandle<HistoryEventNotificationService> container = Arc.container().instance(HistoryEventNotificationService.class)) {
			this.hens = container.get();
		}
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, Stored newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
		
		InventoryItem item;
		try {
			item = this.inventoryItemService.get(oqmDbIdOrName, newOrChangedObject.getItem());
		} catch(DbNotFoundException e) {
			throw new ValidationException("Item " + newOrChangedObject.getItem().toHexString() + " does not exist.", e);
		}
		
		if (!item.getStorageBlocks().contains(newOrChangedObject.getStorageBlock())) {
			throw new ValidationException("Storage block " + newOrChangedObject.getStorageBlock().toHexString() + " not used to hold this item (" + item.getId() + ").");
		}
		
		if (item.getStorageType().storedType != newOrChangedObject.getType()) {
			throw new ValidationException("Stored given of type " + newOrChangedObject.getType() + " cannot be held in item of storage type" + item.getStorageType());
		}
		
		if (item.getStorageType().storedType == StoredType.AMOUNT) {
			if (!item.getUnit().isCompatible(((AmountStored) newOrChangedObject).getAmount().getUnit())) {
				throw new ValidationException("Unit of amount must be compatible with item's unit.");
			}
		}
		
		if (item.getStorageType().storedType == StoredType.AMOUNT && ((AmountStored) newOrChangedObject).getLowStockThreshold() != null) {
			if (!item.getUnit().isCompatible(((AmountStored) newOrChangedObject).getLowStockThreshold().getUnit())) {
				throw new ValidationException("Unit of low stock threshold must be compatible with item's unit.");
			}
		}
		
		if (item.getStorageType() == BULK || item.getStorageType() == UNIQUE_SINGLE) {
			SearchResult<Stored> inBlock = this.search(
				oqmDbIdOrName,
				new StoredSearch()
					.setInventoryItemId(item.getId().toHexString())
					.setStorageBlockId(newOrChangedObject.getStorageBlock().toHexString())
			);
			
			if (!inBlock.isEmpty()) {
				if (inBlock.getNumResults() != 1) {
					throw new ValidationException("More than one stored held for item of type " + item.getStorageType());
				}
				Stored stored = inBlock.getResults().get(0);
				if (newObject || !stored.getId().equals(newOrChangedObject.getId())) {
					throw new ValidationException("Cannot add more than one stored held for type " + item.getStorageType());
				}
			}
		}
		
		if (item.getStorageType() == UNIQUE_SINGLE) {
			SearchResult<Stored> inItem = this.search(oqmDbIdOrName, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
			if (!inItem.isEmpty()) {
				if (inItem.getNumResults() != 1) {
					throw new ValidationException("More than one globally unique stored held");
				}
				
				Stored stored = inItem.getResults().get(0);
				if (newObject || !stored.getId().equals(newOrChangedObject.getId())) {
					throw new ValidationException("Cannot store more than one globally unique stored item.");
				}
			}
		}
		
		for (UniqueId curUniqueId : newOrChangedObject.getUniqueIds()) {
			if (curUniqueId.getType() == UniqueIdType.TO_GENERATE) {
				continue;
			}
			
			List<Stored> uniqueIdresults = this.getItemsWithUniqueId(oqmDbIdOrName, clientSession, curUniqueId, newOrChangedObject.getItem());
			if (!uniqueIdresults.isEmpty()) {
				if (newObject) {
					throw new ValidationException("Item stored with unique id '" + curUniqueId + "' already exists.");
				} else {
					for (Stored curMatcingName : uniqueIdresults) {
						if (!curMatcingName.getId().equals(newOrChangedObject.getId())) {
							throw new ValidationException("Item stored with unique id '" + curUniqueId + "' already exists.");
						}
					}
				}
			}
		}
	}
	
	@Override
	public void massageIncomingData(String oqmDbIdOrName, @NonNull Stored stored) {
		super.massageIncomingData(oqmDbIdOrName, stored);
		
		stored.setGeneralIds(this.getIdentifierGenerationService().replaceIdPlaceholders(oqmDbIdOrName, stored.getGeneralIds()));
		stored.setUniqueIds(this.getIdentifierGenerationService().replaceIdPlaceholders(oqmDbIdOrName, stored.getUniqueIds()));
	}
	
	@Override
	public SearchResult<Stored> search(String oqmDbIdOrName, ClientSession cs, @NonNull StoredSearch searchObject) {
		SearchResult<Stored> results = super.search(oqmDbIdOrName, cs, searchObject);
		
		if (searchObject.getInventoryItemId() != null) {
			results = new ItemAwareSearchResult<>(this.getInventoryItemService().get(oqmDbIdOrName, cs, new ObjectId(searchObject.getInventoryItemId())), results);
		}
		
		return results;
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	public List<Stored> getItemsWithUniqueId(String oqmDbIdOrName, ClientSession clientSession, UniqueId id, ObjectId itemId) {
		Bson filter;
		
		switch (id.getType()) {
			case GENERATED -> {
				filter = and(
					eq("item",  itemId),
					eq("uniqueIds.generatedFrom", ((GeneratedUniqueId) id).getGeneratedFrom()),
					eq("uniqueIds.value", id.getValue())
				);
			}
			case PROVIDED -> {
				filter = and(
					eq("item",  itemId),
					eq("uniqueIds.value", id.getValue())
				);
			}
			default -> {
				return Collections.emptyList();
			}
		}
		
		List<Stored> list = new ArrayList<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			filter,
			null,
			null
		).into(list);
		
		return list;
	}
	
	public <T extends Stored> SearchResult<T> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		StoredSearch search = new StoredSearch()
								  .setInventoryItemId(itemId == null ? null : itemId.toHexString())
								  .setStorageBlockId(storageBlockId == null ? null : storageBlockId.toHexString());
		
		//noinspection unchecked
		SearchResult<T> result = (SearchResult<T>) this.search(
			oqmDbIdOrName,
			cs,
			search
		);
		
		if (result.isEmpty()) {
			throw new DbNotFoundException("No stored currently stored in this block (" + storageBlockId + ") under this item (" + itemId + ").", this.clazz);
		}
		
		return result;
	}
	
	public SearchResult<Stored> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId) {
		return this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, Stored.class);
	}
	
	public <T extends Stored> T getSingleStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		SearchResult<T> result = this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, type);
		
		if (result.getNumResults() != 1) {
			throw new IllegalStateException("Expected single stored in this block ("
											+ storageBlockId
											+ ") under this item ("
											+ itemId
											+ "), but got "
											+ result.getNumResults()
											+ ".");
		}
		
		return result.getResults().getFirst();
	}
	
	
	
	@Override
	public int getCurrentSchemaVersion() {
		return Stored.CUR_SCHEMA_VERSION;
	}
	
	//TODO:: get referencing....
}