package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.collectionStats.InvItemCollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.*;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static tech.ebp.oqm.core.api.model.object.storage.items.StorageType.*;

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
	ItemCheckoutService itemCheckoutService;

	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;

	@Override
	public Set<String> getDisallowedUpdateFields() {
		Set<String> output = new HashSet<>(super.getDisallowedUpdateFields());
		output.add("amount");
		output.add("item");
		output.add("storageBlock");
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
		} catch (DbNotFoundException e) {
			throw new ValidationException("Item " + newOrChangedObject.getItem().toHexString() + " does not exist.", e);
		}

		if (!item.getStorageBlocks().contains(newOrChangedObject.getStorageBlock())) {
			throw new ValidationException("Storage block " + newOrChangedObject.getStorageBlock().toHexString() + " not used to hold this item.");
		}

		if(item.getStorageType().storedType != newOrChangedObject.getStoredType()){
			throw new ValidationException("Stored given of type "+newOrChangedObject.getStoredType()+" cannot be held in item of storage type" + item.getStorageType());
		}

		if(item.getStorageType().storedType == StoredType.AMOUNT){
			if(!item.getUnit().isCompatible(((AmountStored)newOrChangedObject).getAmount().getUnit())){
				throw new ValidationException("Unit of amount must be compatible with item's unit.");
			}
		}

		if (item.getStorageType() == BULK || item.getStorageType() == UNIQUE_SINGLE) {
			SearchResult<Stored> inBlock = this.search(oqmDbIdOrName, new StoredSearch().setInventoryItemId(item.getId()).setStorageBlockId(newOrChangedObject.getStorageBlock()));

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

		if(item.getStorageType() == UNIQUE_SINGLE){
			SearchResult<Stored> inItem = this.search(oqmDbIdOrName, new StoredSearch().setInventoryItemId(item.getId()));
			if (!inItem.isEmpty()) {
				if(inItem.getNumResults() != 1) {
					throw new ValidationException("More than one globally unique stored held");
				}

				Stored stored = inItem.getResults().get(0);
				if (newObject || !stored.getId().equals(newOrChangedObject.getId())) {
					throw new ValidationException("Cannot store more than one globally unique stored item.");
				}
			}
		}
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
			.build();
	}

	public <T extends Stored> SearchResult<T> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		SearchResult<T> result = (SearchResult<T>) this.search(oqmDbIdOrName, cs, new StoredSearch().setInventoryItemId(itemId).setStorageBlockId(storageBlockId));

		if(result.isEmpty()){
			throw new DbNotFoundException("No stored currently stored in this block ("+storageBlockId+") under this item ("+itemId+").", this.clazz);
		}

		return result;
	}

	public SearchResult<Stored> getStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId) {
		return this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, Stored.class);
	}

	public <T extends Stored> T getSingleStoredForItemBlock(String oqmDbIdOrName, ClientSession cs, ObjectId itemId, ObjectId storageBlockId, Class<T> type) {
		SearchResult<T> result = this.getStoredForItemBlock(oqmDbIdOrName, cs, itemId, storageBlockId, type);

		if(result.getNumResults() != 1){
			throw new IllegalStateException("Expected single stored in this block ("+storageBlockId+") under this item ("+itemId+"), but got " + result.getNumResults() + ".");
		}

		return result.getResults().getFirst();
	}

	public StoredStats getItemStats(String oqmDbIdOrName, ClientSession cs, InventoryItem item){
		FindIterable<Stored> storedInItem = this.listIterator(oqmDbIdOrName, cs, and(new StoredSearch().setInventoryItemId(item.getId()).getSearchFilters()), null, null);

		Quantity zero = Quantities.getQuantity(0, item.getUnit());
		Quantity total = Quantities.getQuantity(0, item.getUnit());
		long numStored = 0;
		Map<ObjectId, Long> storageBlockNums = new HashMap<>();
		Map<ObjectId, Quantity> storageBlockTotals = new HashMap<>();
		try(
			MongoCursor<Stored> storedIterator = storedInItem.iterator()
		) {
			while(storedIterator.hasNext()){
				Stored stored = storedIterator.next();
				numStored++;

				Quantity toAdd;
				switch (stored.getStoredType()){
					case AMOUNT -> {
						toAdd = ((AmountStored)stored).getAmount();
					}
					case UNIQUE -> {
						toAdd = UnitUtils.Quantities.UNIT_ONE;
					}
					default -> {
						throw new UnsupportedOperationException("Unsupported stored type (this shouldn't happen): " + stored.getStoredType());
					}
				}
				total = total.add(toAdd);

				ObjectId block = stored.getStorageBlock();
				storageBlockNums.put(block, storageBlockNums.getOrDefault(block, 0L) + 1);
				storageBlockTotals.put(block, storageBlockTotals.getOrDefault(block, zero).add(toAdd));

				//TODO:: expired
				//TODO:: low stock
			}
		}

		Map<ObjectId, StoredInBlockStats> perBlockStats = new LinkedHashMap<>();
		for(ObjectId curBlock : item.getStorageBlocks()){
			long numInBlock = storageBlockNums.getOrDefault(curBlock, 0L);
			Quantity amountInBlock = storageBlockTotals.getOrDefault(curBlock, zero);

			perBlockStats.put(curBlock, StoredInBlockStats.builder()
					.numStored(numInBlock)
					.total(amountInBlock)
				.build());
		}

		//TODO:: low stock

		return StoredStats.builder()
			.total(total)
			.numStored(numStored)
			.storageBlockStats(perBlockStats)
			.build();
	}

	//TODO:: stats on storeds
	//TODO:: add/sub/transfer

	//TODO:: get referencing....
}
