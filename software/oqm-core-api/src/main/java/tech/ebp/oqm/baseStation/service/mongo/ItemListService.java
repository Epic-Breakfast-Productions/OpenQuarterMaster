package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.object.history.events.itemList.ItemListActionAddEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemList;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemListAction;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.baseStation.service.notification.HistoryEventNotificationService;

@Slf4j
@ApplicationScoped
public class ItemListService extends MongoHistoriedObjectService<ItemList, ItemListSearch, CollectionStats> {
	
	ItemListService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ItemListService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		HistoryEventNotificationService hens
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemList.class,
			false,
			hens
		);
	}
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ItemList list, ClientSession clientSession) {
		super.ensureObjectValid(newObject, list, clientSession);
		//TODO:: no duplicate names?
		
	}
	
	public ItemList addAction(ObjectId listId, ObjectId itemId, ItemListAction action, InteractingEntity entity){
		ItemList list = this.get(listId);
		
		list.getItemActions(itemId).add(action);
		
		ItemListActionAddEvent event = new ItemListActionAddEvent(list, entity);
		event.setItemId(itemId);
		
		this.update(list, entity, event);
		
		return list;
	}
	
	public ItemList addAction(String listId, String itemId, ItemListAction action, InteractingEntity entity) {
		return this.addAction(new ObjectId(listId), new ObjectId(itemId), action, entity);
	}
	
	public ItemList remAction(ObjectId listId, ObjectId itemId, int index, InteractingEntity entity){
		ItemList list = this.get(listId);
		//TODO
		return list;
	}
	
	public ItemList remAction(String listId, String itemId, int index, InteractingEntity entity) {
		return this.remAction(new ObjectId(listId), new ObjectId(itemId), index, entity);
	}
	
	public ItemList updateAction(ObjectId listId, ObjectId itemId, int index, JsonObject updateJson, InteractingEntity entity){
		ItemList list = this.get(listId);
		//TODO
		return list;
	}
	
	public ItemList updateAction(String listId, String itemId, int index, JsonObject updateJson, InteractingEntity entity) {
		return this.updateAction(new ObjectId(listId), new ObjectId(itemId), index, updateJson, entity);
	}
	
}
