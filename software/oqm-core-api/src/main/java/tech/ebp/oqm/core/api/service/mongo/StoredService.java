package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;

@Named("StoredService")
@Slf4j
@ApplicationScoped
public class StoredService extends MongoHistoriedObjectService<Stored, StoredSearch, CollectionStats> {

	@Inject
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;

	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemCheckoutService itemCheckoutService;

	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;

	public StoredService() {
		super(Stored.class, false);
		try(InstanceHandle<HistoryEventNotificationService> container = Arc.container().instance(HistoryEventNotificationService.class)){
			this.hens = container.get();
		}
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, Stored newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant, storage block ids exist, image ids exist
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@Override
	public Stored update(String oqmDbIdOrName, Stored object) throws DbNotFoundException {
		/**
		 * TODO:: disallow updating amount
		 */
		Stored item = super.update(oqmDbIdOrName, object);
		return item;
	}
	
	@Override
	public Stored update(String oqmDbIdOrName, ObjectId id, ObjectNode updateJson, InteractingEntity interactingEntity) {
		/**
		 * TODO:: disallow updating amount
		 * also, wat?
		 */
		Stored item = super.update(oqmDbIdOrName, id, updateJson, interactingEntity);
		super.update(oqmDbIdOrName, item);
		
		return item;
	}

	//TODO:: stats on storeds
	//TODO:: add/sub/transfer

	//TODO:: get referencing....
}
