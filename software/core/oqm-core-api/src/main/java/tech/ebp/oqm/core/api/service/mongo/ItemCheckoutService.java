package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.ItemCheckoutSearch;

import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

/**
 *
 */
@Named("ItemCheckoutService")
@Slf4j
@ApplicationScoped
public class ItemCheckoutService extends MongoHistoriedObjectService<ItemCheckout, ItemCheckoutSearch, CollectionStats> {
	
	@Inject
	InventoryItemService inventoryItemService;
	
	public ItemCheckoutService() {
		super(ItemCheckout.class, false);
	}
	
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, ItemCheckout newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
		//TODO:: this
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}

	public Set<ObjectId> getItemCheckoutsReferencing(String oqmDbIdOrName, ClientSession clientSession, StorageBlock storageBlock){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			or(
				eq("checkedOutFrom", storageBlock.getId()),
				eq("checkinDetails.storageBlockCheckedInto", storageBlock.getId())
			),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getItemCheckoutsReferencing(String oqmDbIdOrName, ClientSession clientSession, InventoryItem item){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("item", item.getId()),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getItemCheckoutsReferencing(String oqmDbIdOrName, ClientSession clientSession, InteractingEntity entity){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("checkedOutFor.userId", entity.getId()),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return ItemCheckout.CUR_SCHEMA_VERSION;
	}
}
