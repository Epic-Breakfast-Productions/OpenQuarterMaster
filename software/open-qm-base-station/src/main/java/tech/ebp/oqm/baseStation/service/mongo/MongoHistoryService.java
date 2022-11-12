package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbHistoryNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistory;
import tech.ebp.oqm.lib.core.object.history.events.DeleteEvent;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.user.User;

import javax.validation.Valid;

import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
@Traced
public class MongoHistoryService<T extends MainObject> extends MongoService<ObjectHistory, HistorySearch> {
	
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
			ObjectHistory.class,
			null
		);
		this.clazzForObjectHistoryIsFor = clazz;
	}
	
	public ObjectHistory getHistoryFor(ClientSession clientSession, ObjectId id) {
		ObjectHistory found;
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
		if (found == null) {
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		return found;
	}
	
	public ObjectHistory getHistoryFor(ObjectId id) {
		return this.getHistoryFor(null, id);
	}
	
	public ObjectHistory getHistoryFor(ClientSession clientSession, T object) {
		return this.getHistoryFor(clientSession, object.getId());
	}
	
	public ObjectHistory getHistoryFor(T object) {
		return this.getHistoryFor(null, object);
	}
	
	public ObjectId createHistoryFor(ClientSession session, T created, InteractingEntity entity) {
		try {
			this.getHistoryFor(session, created);
			throw new IllegalStateException(
				"History already exists for object " + this.clazzForObjectHistoryIsFor.getSimpleName() + " with id: " + created.getId()
			);
		} catch(DbNotFoundException e) {
			// no history record should exist.
		}
		
		ObjectHistory history = new ObjectHistory(created, entity);
		
		return this.add(session, history);
	}
	
	public ObjectId createHistoryFor(T created, User user) {
		return this.createHistoryFor(null, created, user);
	}
	
	public ObjectHistory addHistoryEvent(ClientSession clientSession, ObjectId objectId, @Valid HistoryEvent event) {
		ObjectHistory history;
		try {
			history = this.getHistoryFor(clientSession, objectId);
		} catch(DbNotFoundException e) {
			log.error("Could not find history for object! (Should not happen)");
			throw e;
		}
		
		history.updated(event);
		this.update(clientSession, history);
		
		return history;
	}
	
	public ObjectHistory addHistoryEvent(ObjectId objectId, @Valid HistoryEvent event) {
		return addHistoryEvent(null, objectId, event);
	}
	
	public ObjectHistory updateHistoryFor(ClientSession clientSession, T updated, InteractingEntity entity, ObjectNode updateJson,
										  String description) {
		return this.addHistoryEvent(
			clientSession,
			updated.getId(),
			UpdateEvent.builder()
					   .entityId(entity.getId())
					   .entityType(entity.getInteractingEntityType())
					   .fieldsUpdated(UpdateEvent.fieldListFromJson(updateJson))
					   .description(description)
					   .build()
		);
	}
	
	public ObjectHistory updateHistoryFor(T updated, InteractingEntity entity, ObjectNode updateJson, String description) {
		return this.updateHistoryFor(null, updated, entity, updateJson, description);
	}
	
	
	public ObjectHistory updateHistoryFor(ClientSession clientSession, T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.updateHistoryFor(updated, entity, updateJson, "");
	}
	
	public ObjectHistory updateHistoryFor(T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.updateHistoryFor(null, updated, entity, updateJson);
	}
	
	public ObjectHistory objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity, String description) {
		return this.addHistoryEvent(
			clientSession,
			updated.getId(),
			DeleteEvent.builder()
					   .entityId(entity.getId())
					   .entityType(entity.getInteractingEntityType())
					   .description(description)
					   .build()
		);
	}
	
	public ObjectHistory objectDeleted(T updated, InteractingEntity entity, String description) {
		return this.objectDeleted(null, updated, entity, description);
	}
	
	public ObjectHistory objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity) {
		return this.objectDeleted(clientSession, updated, entity, "");
	}
	
	public ObjectHistory objectDeleted(T updated, InteractingEntity entity) {
		return this.objectDeleted(null, updated, entity);
	}
}
