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
	
	public ObjectHistory getHistoryFor(ObjectId id) {
		ObjectHistory found = getCollection()
								  .find(eq("objectId", id))
								  .limit(1)
								  .first();
		if (found == null) {
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		return found;
	}
	
	public ObjectHistory getHistoryFor(T object) {
		return this.getHistoryFor(object.getId());
	}
	
	public ObjectId createHistoryFor(ClientSession session, T created, User user) {
		try {
			this.getHistoryFor(created);
			throw new IllegalStateException(
				"History already exists for object " + this.clazzForObjectHistoryIsFor.getSimpleName() + " with id: " + created.getId()
			);
		} catch(DbNotFoundException e){
			// no history record should exist.
		}
		
		ObjectHistory history = new ObjectHistory(created, user);
		
		return this.add(session, history);
	}
	
	public ObjectId createHistoryFor(T created, User user) {
		return this.createHistoryFor(null, created, user);
	}
	
	public ObjectHistory addHistoryEvent(ObjectId objectId, @Valid HistoryEvent event) {
		ObjectHistory history;
		try {
			history = this.getHistoryFor(objectId);
		} catch(DbNotFoundException e) {
			log.error("Could not find history for object! (Should not happen)");
			throw e;
		}
		
		history.updated(event);
		
		this.getCollection().findOneAndReplace(
			eq("_id", history.getId()),
			history
		);
		return history;
	}
	
	public ObjectHistory updateHistoryFor(T updated, User user, ObjectNode updateJson, String description) {
		return this.addHistoryEvent(
			updated.getId(),
			UpdateEvent.builder()
					   .userId(user.getId())
					   .fieldsUpdated(UpdateEvent.fieldListFromJson(updateJson))
					   .description(description)
					   .build()
		);
	}
	
	public ObjectHistory updateHistoryFor(T updated, User user, ObjectNode updateJson) {
		return this.updateHistoryFor(updated, user, updateJson, "");
	}
	
	public ObjectHistory objectDeleted(T updated, User user, String description) {
		return this.addHistoryEvent(
			updated.getId(),
			DeleteEvent.builder()
					   .userId(user.getId())
					   .description(description)
					   .build()
		);
	}
	public ObjectHistory objectDeleted(T updated, User user) {
		return this.objectDeleted(updated, user, "");
	}
}
