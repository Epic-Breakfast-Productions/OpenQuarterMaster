package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.collectionStats.InvItemCollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
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
			throw new ValidationException("Storage block " + newOrChangedObject.getStorageBlock().toHexString() + " not use to hold this item.");
		}

		if(item.getStorageType().storedType != newOrChangedObject.getStoredType()){
			throw new ValidationException("Stored given of type "+newOrChangedObject.getStoredType()+" cannot be held in item of storage type" + item.getStorageType());
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

		if(item.getStorageType() == UNIQUE_GLOBAL){
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

	@Override
	public Stored update(String oqmDbIdOrName, Stored object) throws DbNotFoundException {
		Stored item = super.update(oqmDbIdOrName, object);
		return item;
	}

	@Override
	public Stored update(String oqmDbIdOrName, ObjectId id, ObjectNode updateJson, InteractingEntity interactingEntity) {
		Stored item = super.update(oqmDbIdOrName, id, updateJson, interactingEntity);
		return item;
	}

	//TODO:: stats on storeds
	//TODO:: add/sub/transfer

	//TODO:: get referencing....
}
