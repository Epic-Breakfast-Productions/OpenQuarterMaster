package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.baseStation.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemCheckinEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemCheckoutEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.checkout.CheckInDetails;
import tech.ebp.oqm.lib.core.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.storage.itemCheckout.ItemCheckinRequest;
import tech.ebp.oqm.lib.core.rest.storage.itemCheckout.ItemCheckoutRequest;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTreeNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.eq;

@Named("ItemCheckoutService")
@Slf4j
@ApplicationScoped
public class ItemCheckoutService extends MongoHistoriedObjectService<ItemCheckout, ItemCheckoutSearch> {
	
	private InventoryItemService inventoryItemService;
	
	ItemCheckoutService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ItemCheckoutService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		InventoryItemService inventoryItemService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemCheckout.class,
			false
		);
		this.inventoryItemService = inventoryItemService;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ItemCheckout newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: this
	}
	
	public ObjectId checkoutItem(ItemCheckoutRequest request, InteractingEntity entity){
		InventoryItem item = this.inventoryItemService.get(request.getItem());
		
		Stored result;
		try {
			result = item.subtract(request.getCheckedOutFrom(), request.getToCheckout());
		} catch(ClassCastException e){
			throw new IllegalArgumentException("Bad stored type given for item.", e);
		}
		
		ItemCheckout itemCheckout = new ItemCheckout();
		itemCheckout.setCheckedOut(result);
		itemCheckout.setItem(request.getItem());
		itemCheckout.setCheckedOutFrom(request.getCheckedOutFrom());
		itemCheckout.setCheckedOutFor(request.getCheckedOutFor());
		itemCheckout.setDueBack(request.getDueBack());
		itemCheckout.setReason(request.getReason());
		itemCheckout.setNotes(request.getNotes());
		
		ObjectId newId;
		try(ClientSession cs = this.getNewClientSession(true)){
			newId = this.add(cs, itemCheckout, entity);
			this.inventoryItemService.update(cs, item, entity, new ItemCheckoutEvent(item, entity).setItemCheckoutId(newId));
		}
		
		return newId;
	}
	
	public ItemCheckout checkinItem(ObjectId checkoutId, ItemCheckinRequest request, InteractingEntity entity) {
		ItemCheckout checkout = this.get(checkoutId);
		InventoryItem item = this.inventoryItemService.get(checkout.getItem());
		
		CheckInDetails.Builder checkInDetailBuilder = CheckInDetails.fromRequest(request);
		
		if(request.getStorageBlockCheckedInto() == null){
			checkInDetailBuilder.storageBlockCheckedInto(checkout.getCheckedOutFrom());
		}
		
		checkout.setCheckInDetails(checkInDetailBuilder.build());
		item.add(checkout.getCheckInDetails().getStorageBlockCheckedInto(), checkout.getCheckedOut(), false);
		
		try(ClientSession cs = this.getNewClientSession(true)){
			this.inventoryItemService.update(cs, item, entity, new ItemCheckinEvent(item, entity).setItemCheckoutId(checkout.getId()));
			this.update(checkout, entity, new UpdateEvent(checkout, entity).setDescription("Checkin"));
		}
		
		return checkout;
	}
	
	//TODO:: prevent updates to those that are already checked in
	
	public Set<ObjectId> getItemCheckoutsReferencing(ClientSession clientSession, StorageBlock storageBlock){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			clientSession,
			eq("checkedOutFrom", storageBlock.getId()),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getItemCheckoutsReferencing(ClientSession clientSession, InventoryItem<?, ?, ?> item){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			clientSession,
			eq("item", item.getId()),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getItemCheckoutsReferencing(ClientSession clientSession, InteractingEntity entity){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			clientSession,
			eq("checkedOutFor.userId", entity.getId()),
			null,
			null
		).map(MainObject::getId).into(list);
		return list;
	}
}
