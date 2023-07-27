package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbHistoryNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.CreateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public class MongoHistoryService<T extends MainObject> extends MongoObjectService<ObjectHistoryEvent, HistorySearch> {
	
	public static final String COLLECTION_HISTORY_APPEND = "-history";
	
	private final Class<T> clazzForObjectHistoryIsFor;
	
	public MongoHistoryService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			getCollectionNameFromClass(clazz) + COLLECTION_HISTORY_APPEND,
			ObjectHistoryEvent.class,
			null
		);
		this.clazzForObjectHistoryIsFor = clazz;
	}
	
	@WithSpan
	public DeleteEvent isDeleted(ClientSession clientSession, ObjectId id) {
		DeleteEvent found;
		
		Bson search = and(
			eq("objectId", id),
			eq("type", EventType.DELETE)
		);
		
		if (clientSession != null) {
			found = (DeleteEvent) getCollection()
						.find(clientSession, search)
						.limit(1)
						.first();
		} else {
			found = (DeleteEvent) getCollection()
						.find(search)
						.limit(1)
						.first();
		}
		
		if(found == null){
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		
		return found;
	}
	
	public DeleteEvent isDeleted(ObjectId id) {
		return this.isDeleted(null, id);
	}
	
	@WithSpan
	public ObjectHistoryEvent getLatestHistoryEventFor(ClientSession clientSession, ObjectId id) {
		ObjectHistoryEvent found;
		if (clientSession != null) {
			found = getCollection()
						.find(clientSession, eq("objectId", id))
						.limit(1)
						.first();
		} else {
			found = getCollection()
						.find(eq("objectId", id))
						.limit(1)
						.first();
		}
		
		if(found == null){
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		
		return found;
	}
	
	public ObjectHistoryEvent getLatestHistoryEventFor(ObjectId id) {
		return this.getLatestHistoryEventFor(null, id);
	}
	
	@WithSpan
	public boolean hasHistoryFor(ClientSession clientSession, ObjectId id) {
		ObjectHistoryEvent found;
		if (clientSession != null) {
			found = getCollection()
						.find(clientSession, eq("objectId", id))
						.limit(1)
						.first();
		} else {
			found = getCollection()
						.find(eq("objectId", id))
						.limit(1)
						.first();
		}
		return found != null;
	}
	
	public boolean hasHistoryFor(ObjectId id) {
		return this.hasHistoryFor(null, id);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(ClientSession clientSession, ObjectId id) {
		List<ObjectHistoryEvent> output = this.list(
			clientSession,
			eq("objectId", id),
			null,
			null
		);
		
		if(output.isEmpty()){
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		return output;
	}
	
	public List<ObjectHistoryEvent> getHistoryFor(ObjectId id) {
		return this.getHistoryFor(null, id);
	}
	
	public List<ObjectHistoryEvent> getHistoryFor(ClientSession clientSession, T object) {
		return this.getHistoryFor(clientSession, object.getId());
	}
	
	public List<ObjectHistoryEvent> getHistoryFor(T object) {
		return this.getHistoryFor(null, object);
	}
	
	@WithSpan
	public ObjectId addHistoryFor(ClientSession session, T created, InteractingEntity entity, ObjectHistoryEvent history){
		history.setObjectId(created.getId());
		if(entity != null) {
			history.setEntity(entity.getReference());
		}
		
		return this.add(session, history);
	}
	public ObjectId addHistoryFor(T created, InteractingEntity entity, ObjectHistoryEvent history){
		return this.addHistoryFor(null, created, entity, history);
	}
	
	@WithSpan
	public ObjectId objectCreated(ClientSession session, T created, InteractingEntity entity) {
		try {
			this.getHistoryFor(session, created);
			throw new IllegalStateException(
				"History already exists for object " + this.clazzForObjectHistoryIsFor.getSimpleName() + " with id: " + created.getId()
			);
		} catch(DbNotFoundException e) {
			// no history record should exist.
		}
		
		ObjectHistoryEvent history = new CreateEvent(created, entity);
		
		return this.addHistoryFor(session, created, entity, history);
	}
	
	public ObjectId objectCreated(T created, InteractingEntity interactingEntity) {
		return this.objectCreated(null, created, interactingEntity);
	}
	
	@WithSpan
	public ObjectId objectUpdated(
		ClientSession clientSession,
		T updated,
		InteractingEntity entity,
		ObjectNode updateJson,
		String description
	) {
		
		UpdateEvent event = new UpdateEvent(updated, entity);
		
		if (updateJson != null) {
			event.setFieldsUpdated(ObjectUtils.fieldListFromJson(updateJson));
		}
		if(description != null && !description.isBlank()){
			event.setDescription(description);
		}
		
		return this.addHistoryFor(clientSession, updated, entity, event);
	}
	
	@Override
	public FindIterable<ObjectHistoryEvent> listIterator(ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		return super.listIterator(clientSession, filter, (sort != null ? sort : Sorts.descending("timestamp")), pageOptions);
	}
	
	public ObjectId objectUpdated(T updated, InteractingEntity entity, ObjectNode updateJson, String description) {
		return this.objectUpdated(null, updated, entity, updateJson, description);
	}
	
	
	public ObjectId objectUpdated(ClientSession clientSession, T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(clientSession, updated, entity, updateJson, "");
	}
	
	public ObjectId objectUpdated(T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(null, updated, entity, updateJson);
	}
	
	@WithSpan
	public ObjectId objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity, String description) {
		DeleteEvent event = new DeleteEvent(updated, entity);
		
		if(description != null && !description.isBlank()){
			event.setDescription(description);
		}
		
		return this.addHistoryFor(clientSession, updated, entity, event);
	}
	
	public ObjectId objectDeleted(T updated, InteractingEntity entity, String description) {
		return this.objectDeleted(null, updated, entity, description);
	}
	
	public ObjectId objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity) {
		return this.objectDeleted(clientSession, updated, entity, "");
	}
	
	public ObjectId objectDeleted(T updated, InteractingEntity entity) {
		return this.objectDeleted(null, updated, entity);
	}
}
