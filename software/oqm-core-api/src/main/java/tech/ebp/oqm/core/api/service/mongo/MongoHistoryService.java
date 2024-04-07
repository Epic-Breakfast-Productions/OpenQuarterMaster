package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.HistoryCollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbHistoryNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.service.serviceState.db.MongoDatabaseService;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public class MongoHistoryService<T extends MainObject> extends MongoObjectService<ObjectHistoryEvent, HistorySearch, HistoryCollectionStats> {
	
	public static final String COLLECTION_HISTORY_APPEND = "-history";
	
	private final Class<T> clazzForObjectHistoryIsFor;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;
	
	public MongoHistoryService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		MongoDatabaseService mongoDatabaseService,
		Class<T> clazzForObjectHistoryIsFor,
		HistoryEventNotificationService hens
	) {
		super(objectMapper, mongoClient, database, mongoDatabaseService, getCollectionNameFromClass(clazzForObjectHistoryIsFor) + COLLECTION_HISTORY_APPEND, ObjectHistoryEvent.class);
		this.clazzForObjectHistoryIsFor = clazzForObjectHistoryIsFor;
		this.hens = hens;
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
	public ObjectId addHistoryFor(ClientSession session, T objectReferred, InteractingEntity entity, ObjectHistoryEvent history){
		history.setObjectId(objectReferred.getId());
		if(entity != null) {
			history.setEntity(entity.getId());
		}
		ObjectId output = this.add(session, history);
		this.getHens().sendEvents(this.clazzForObjectHistoryIsFor, history);
		return output;
	}
	public ObjectId addHistoryFor(T objectReferred, InteractingEntity entity, ObjectHistoryEvent history){
		return this.addHistoryFor(null, objectReferred, entity, history);
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
	
	@Override
	public HistoryCollectionStats getStats() {
		return super.addBaseStats(HistoryCollectionStats.builder())
				   //TODO:: this
				   .build();
	}
	
}
