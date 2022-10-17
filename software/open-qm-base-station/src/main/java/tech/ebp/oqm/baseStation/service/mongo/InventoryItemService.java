package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemAddEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemSubEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemTransferEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;

/**
 * TODO::
 *    - Figure out how to handle expired state when adding, updating
 */
@Traced
@Slf4j
@ApplicationScoped
public class InventoryItemService extends MongoHistoriedService<InventoryItem, InventoryItemSearch> {
	
	InventoryItemService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	InventoryItemService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			InventoryItem.class,
			false
		);
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, InventoryItem newOrChangedObject, ClientSession clientSession) {
		newOrChangedObject.recalculateDerived();
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant, storage block ids exist, image ids exist
	}
	
	private <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockId,
		T toAdd
	) {
		this.get(item.getId());//ensure exists
		try {
			item.add(storageBlockId, toAdd, true);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		Object result = this.getCollection().findOneAndReplace(eq("_id", item.getId()), item);
		
		{
			ItemAddEvent.Builder<?, ?> builder = ItemAddEvent.builder().storageBlockId(storageBlockId);
			
			this.getHistoryService().addHistoryEvent(
				item.getId(),
				builder.build()
			);
			
		}
		
		return item;
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(
		ObjectId itemId,
		ObjectId storageBlockId,
		T toAdd
	) {
		return this.add(this.get(itemId), storageBlockId, toAdd);
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> add(String itemId, String storageBlockId, T toAdd) {
		return this.add(new ObjectId(itemId), new ObjectId(storageBlockId), toAdd);
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockId,
		T toSubtract
	) {
		this.get(item.getId());//ensure exists
		try {
			item.subtract(storageBlockId, toSubtract);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		Object result = this.getCollection().findOneAndReplace(eq("_id", item.getId()), item);
		
		{
			ItemSubEvent.Builder<?, ?> builder = ItemSubEvent.builder().storageBlockId(storageBlockId);
			
			this.getHistoryService().addHistoryEvent(
				item.getId(),
				builder.build()
			);
		}
		
		return item;
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		ObjectId itemId,
		ObjectId storageBlockId,
		T toAdd
	) {
		return this.subtract(this.get(itemId), storageBlockId, toAdd);
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> subtract(
		String itemId,
		String storageBlockId,
		T toAdd
	) {
		return this.subtract(new ObjectId(itemId), new ObjectId(storageBlockId), toAdd);
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		InventoryItem<T, C, W> item,
		ObjectId storageBlockIdFrom,
		ObjectId storageBlockIdTo,
		T toTransfer
	) {
		this.get(item.getId());//ensure exists
		try {
			item.transfer(storageBlockIdFrom, storageBlockIdTo, toTransfer);
		} catch(ClassCastException e) {
			//not given proper stored type
			//TODO:: custom exception
			throw e;
		}
		
		Object result = this.getCollection().findOneAndReplace(eq("_id", item.getId()), item);
		
		{
			ItemTransferEvent.Builder<?, ?> builder =
				ItemTransferEvent.builder().storageBlockFromId(storageBlockIdFrom).storageBlockToId(storageBlockIdTo);
			
			this.getHistoryService().addHistoryEvent(
				item.getId(),
				builder.build()
			);
		}
		
		return item;
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		ObjectId itemId,
		ObjectId storageBlockIdFrom,
		ObjectId storageBlockIdTo,
		T toTransfer
	) {
		return this.transfer(
			this.get(itemId),
			storageBlockIdFrom,
			storageBlockIdTo,
			toTransfer
		);
	}
	
	public <T extends Stored, C, W extends StoredWrapper<C, T>> InventoryItem<T, C, W> transfer(
		String itemId,
		String storageBlockIdFrom,
		String storageBlockIdTo,
		T toTransfer
	) {
		return this.transfer(
			new ObjectId(itemId),
			new ObjectId(storageBlockIdFrom),
			new ObjectId(storageBlockIdTo),
			toTransfer
		);
	}
	
	public List<InventoryItem> getItemsInBlock(ObjectId storageBlockId) {
		return this.list(
			exists("storageMap." + storageBlockId.toHexString()),
			null,
			null
		);
	}
	
	public List<InventoryItem> getItemsInBlock(String storageBlockId) {
		return this.getItemsInBlock(new ObjectId(storageBlockId));
	}
	
	public long getNumStoredExpired() {
		return this.getSumOfIntField("numExpired");
	}
	
	public long getNumStoredExpiryWarn() {
		return this.getSumOfIntField("numExpiryWarn");
	}
	
}
