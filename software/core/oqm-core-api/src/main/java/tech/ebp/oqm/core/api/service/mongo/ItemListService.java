package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.events.itemList.ItemListActionAddEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.itemList.ItemList;
import tech.ebp.oqm.core.api.model.object.itemList.ItemListAction;
import tech.ebp.oqm.core.api.model.rest.search.ItemListSearch;

@Slf4j
@ApplicationScoped
public class ItemListService extends MongoHistoriedObjectService<ItemList, ItemListSearch, CollectionStats> {
	
	public ItemListService() {
		super(ItemList.class, false);
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, ItemList list, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, list, clientSession);
		//TODO:: no duplicate names?
		
	}
	
	public ItemList addAction(String oqmDbIdOrName, ObjectId listId, ObjectId itemId, ItemListAction action, InteractingEntity entity) {
		ItemList list = this.get(oqmDbIdOrName, listId);
		
		list.getItemActions(itemId).add(action);
		
		ItemListActionAddEvent event = new ItemListActionAddEvent(list, entity);
		event.setItemId(itemId);
		
//		this.update(oqmDbIdOrName, null, list, entity, event); //TODO:: add back in
		
		return list;
	}
	
	public ItemList addAction(String oqmDbIdOrName, String listId, String itemId, ItemListAction action, InteractingEntity entity) {
		return this.addAction(oqmDbIdOrName, new ObjectId(listId), new ObjectId(itemId), action, entity);
	}
	
	public ItemList remAction(String oqmDbIdOrName, ObjectId listId, ObjectId itemId, int index, InteractingEntity entity) {
		ItemList list = this.get(oqmDbIdOrName, listId);
		//TODO
		return list;
	}
	
	public ItemList remAction(String oqmDbIdOrName, String listId, String itemId, int index, InteractingEntity entity) {
		return this.remAction(oqmDbIdOrName, new ObjectId(listId), new ObjectId(itemId), index, entity);
	}
	
	public ItemList updateAction(String oqmDbIdOrName, ObjectId listId, ObjectId itemId, int index, JsonObject updateJson, InteractingEntity entity) {
		ItemList list = this.get(oqmDbIdOrName, listId);
		//TODO
		return list;
	}
	
	public ItemList updateAction(String oqmDbIdOrName, String listId, String itemId, int index, JsonObject updateJson, InteractingEntity entity) {
		return this.updateAction(oqmDbIdOrName, new ObjectId(listId), new ObjectId(itemId), index, updateJson, entity);
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return ItemList.CUR_SCHEMA_VERSION;
	}
}
