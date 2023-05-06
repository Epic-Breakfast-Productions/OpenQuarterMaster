package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.config.BaseStationInteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.notification.item.ItemLowStockEventNotificationService;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemAddEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemSubEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemTransferEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.or;
import static tech.ebp.oqm.lib.core.rest.media.ObjectCodeContentType.id;

/**
 * TODO::
 *    - Figure out how to handle expired state when adding, updating
 */
@Slf4j
@ApplicationScoped
public class InventoryItemService extends MongoHistoriedObjectService<InventoryItem, InventoryItemSearch> {
	
	private BaseStationInteractingEntity baseStationInteractingEntity;
	private ItemLowStockEventNotificationService ilsens;
	
	InventoryItemService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	InventoryItemService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		ItemLowStockEventNotificationService ilsens,
		BaseStationInteractingEntity baseStationInteractingEntity
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			InventoryItem.class,
			false
		);
		this.baseStationInteractingEntity = baseStationInteractingEntity;
		this.ilsens = ilsens;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, InventoryItem newOrChangedObject, ClientSession clientSession) {
		newOrChangedObject.recalculateDerived();
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant, storage block ids exist, image ids exist
	}
	
	@WithSpan
	private void handleLowStockEvents(InventoryItem item, List<ItemLowStockEvent> lowStockEvents) {
		if (!lowStockEvents.isEmpty()) {
			for (ItemLowStockEvent event : lowStockEvents) {
				
				event.setEntity(this.baseStationInteractingEntity.getReference());
				
				this.getHistoryService().addHistoryFor(
					item, null, event
				);
			}
			this.ilsens.sendEvents(item, lowStockEvents);
		}
	}
	
	@Override
	public InventoryItem update(InventoryItem object) throws DbNotFoundException {
		List<ItemLowStockEvent> lowStockEvents = object.updateLowStockState();
		InventoryItem item = super.update(object);
		
		handleLowStockEvents(item, lowStockEvents);
		
		return item;
	}
	
	@Override
	public InventoryItem update(ObjectId id, ObjectNode updateJson, InteractingEntity interactingEntity) {
		InventoryItem item = super.update(id, updateJson, interactingEntity);
		
		List<ItemLowStockEvent> lowStockEvents = item.updateLowStockState();
		
		super.update(item);
		if (!lowStockEvents.isEmpty()) {
			this.handleLowStockEvents(item, lowStockEvents);
		}
		
		return item;
	}
	
	@WithSpan
	private <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockId,
		T toAdd,
		@Valid
		InteractingEntity entity
	) {
		this.get(item.getId());//ensure exists
		try {
			item.add(storageBlockId, toAdd, true);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		
		this.update(
			item,
			entity,
			new ItemAddEvent(item, entity)
				.setStorageBlockId(storageBlockId)//TODO:: add quantity?
		);
		
		return item;
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(
		ObjectId itemId,
		ObjectId storageBlockId,
		T toAdd,
		@NotNull
		InteractingEntity entity
	) {
		return this.add(this.get(itemId), storageBlockId, toAdd, entity);
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(
		String itemId,
		String storageBlockId,
		T toAdd,
		@NotNull
		InteractingEntity entity
	) {
		return this.add(new ObjectId(itemId), new ObjectId(storageBlockId), toAdd, entity);
	}
	
	@WithSpan
	private <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockId,
		T toSubtract,
		InteractingEntity entity
	) {
		this.get(item.getId());//ensure exists
		try {
			item.subtract(storageBlockId, toSubtract);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		this.update(
			item,
			entity,
			new ItemSubEvent(item, entity)
				.setStorageBlockId(storageBlockId)//TODO:: add quantity?
		);
		
		return item;
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		ObjectId itemId,
		ObjectId storageBlockId,
		T toAdd,
		@NotNull
		InteractingEntity entity
	) {
		return this.subtract(this.get(itemId), storageBlockId, toAdd, entity);
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		String itemId,
		String storageBlockId,
		T toAdd,
		@NotNull
		InteractingEntity entity
	) {
		return this.subtract(new ObjectId(itemId), new ObjectId(storageBlockId), toAdd, entity);
	}
	
	@WithSpan
	private <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockIdFrom,
		ObjectId storageBlockIdTo,
		T toTransfer,
		@NotNull
		InteractingEntity entity
	) {
		this.get(item.getId());//ensure exists
		try {
			item.transfer(storageBlockIdFrom, storageBlockIdTo, toTransfer);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		this.update(
			item,
			entity,
			new ItemTransferEvent(item, entity)
				.setStorageBlockFromId(storageBlockIdFrom)
				.setStorageBlockToId(storageBlockIdTo)//TODO:: add quantity?
		);
		
		return item;
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		ObjectId itemId,
		ObjectId storageBlockIdFrom,
		ObjectId storageBlockIdTo,
		T toTransfer,
		@NotNull
		InteractingEntity entity
	) {
		return this.transfer(
			this.get(itemId),
			storageBlockIdFrom,
			storageBlockIdTo,
			toTransfer,
			entity
		);
	}
	
	@WithSpan
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		String itemId,
		String storageBlockIdFrom,
		String storageBlockIdTo,
		T toTransfer,
		@NotNull
		InteractingEntity entity
	) {
		return this.transfer(
			new ObjectId(itemId),
			new ObjectId(storageBlockIdFrom),
			new ObjectId(storageBlockIdTo),
			toTransfer,
			entity
		);
	}
	
	@WithSpan
	public List<InventoryItem> getItemsInBlock(ObjectId storageBlockId) {
		return this.list(
			exists("storageMap." + storageBlockId.toHexString()),
			null,
			null
		);
	}
	
	@WithSpan
	public List<InventoryItem> getItemsInBlock(String storageBlockId) {
		return this.getItemsInBlock(new ObjectId(storageBlockId));
	}
	
	@WithSpan
	public long getNumStoredExpired() {
		return this.getSumOfIntField("numExpired");
	}
	
	@WithSpan
	public long getNumStoredExpiryWarn() {
		return this.getSumOfIntField("numExpiryWarn");
	}
	
	@WithSpan
	public long getNumLowStock() {
		return this.getSumOfIntField("numLowStock");
	}
	
	public Set<ObjectId> getItemsReferencing(ClientSession clientSession, Image image) {
		// { "imageIds": {$elemMatch: {$eq:ObjectId('6335f3c338a79a4377aea064')}} }
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			clientSession,
			eq("imageIds", image.getId()),
			null,
			null
		).map(InventoryItem::getId).into(list);
		
		//TODO:: figure out how to wrap this into the previous query; finding image in stored entries
		this.listIterator(clientSession).forEach((InventoryItem item)->{
			item.getStorageMap().forEach((storageBlockId, storedWrapper)->{
				List<ObjectId> imageIds;
				if(storedWrapper instanceof SingleAmountStoredWrapper){
					imageIds = ((SingleAmountStoredWrapper) storedWrapper).getStored().getImageIds();
				} else if(storedWrapper instanceof ListAmountStoredWrapper){
					imageIds = ((ListAmountStoredWrapper) storedWrapper).stream().map(AmountStored::getImageIds).flatMap(List::stream).collect(Collectors.toList());
				} else if(storedWrapper instanceof TrackedMapStoredWrapper){
					imageIds = ((TrackedMapStoredWrapper) storedWrapper).storedStream().map(TrackedStored::getImageIds).flatMap(List::stream).collect(Collectors.toList());
				} else {
					throw new IllegalStateException("Should not get here.");
				}
				if(imageIds.contains(image.getId())){
					list.add(item.getId());
				}
			});
		});
		
		return list;
	}
	
	public Set<ObjectId> getItemsReferencing(ClientSession clientSession, StorageBlock storageBlock) {
		Set<ObjectId> list = new TreeSet<>();
		
		//TODO:: figure out how find with query
		this.listIterator(clientSession).forEach((InventoryItem item)->{
			if(item.getStorageMap().containsKey(storageBlock.getId())){
				list.add(item.getId());
			}
		});
		
		return list;
	}
}
