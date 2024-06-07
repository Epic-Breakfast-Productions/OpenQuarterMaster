package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
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
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * TODO:: update this to be injected, one instance to handle all history. Pull from higher up than object service? OR programmatically get Hens: Arc.container().instance(Foo.class).get()
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public class MongoHistoryService<T extends MainObject> extends MongoObjectService<ObjectHistoryEvent, HistorySearch, HistoryCollectionStats> {
	
	public static final String COLLECTION_HISTORY_APPEND = "-history";
	
	private final Class<T> clazzForObjectHistoryIsFor;
	
	@Getter(AccessLevel.PRIVATE)
	HistoryEventNotificationService hens;
	
	public MongoHistoryService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		OqmDatabaseService oqmDatabaseService,
		Class<T> clazzForObjectHistoryIsFor
	) {
		super(objectMapper, mongoClient, database, oqmDatabaseService, getCollectionNameFromClass(clazzForObjectHistoryIsFor) + COLLECTION_HISTORY_APPEND, ObjectHistoryEvent.class);
		this.clazzForObjectHistoryIsFor = clazzForObjectHistoryIsFor;
		try(InstanceHandle<HistoryEventNotificationService> container = Arc.container().instance(HistoryEventNotificationService.class)){
			this.hens = container.get();
		}
	}
	
	@WithSpan
	public DeleteEvent isDeleted(String oqmDbIdOrName, ClientSession clientSession, ObjectId id) {
		MongoCollection<ObjectHistoryEvent> collection = this.getCollection(oqmDbIdOrName);
		DeleteEvent found;
		
		Bson search = and(
			eq("objectId", id),
			eq("type", EventType.DELETE)
		);
		
		if (clientSession != null) {
			found = (DeleteEvent) collection
						.find(clientSession, search)
						.limit(1)
						.first();
		} else {
			found = (DeleteEvent) collection
						.find(search)
						.limit(1)
						.first();
		}
		
		if(found == null){
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		
		return found;
	}
	
	public DeleteEvent isDeleted(String oqmDbIdOrName, ObjectId id) {
		return this.isDeleted(oqmDbIdOrName, null, id);
	}
	
	@WithSpan
	public ObjectHistoryEvent getLatestHistoryEventFor(String oqmDbIdOrName, ClientSession clientSession, ObjectId id) {
		ObjectHistoryEvent found;
		MongoCollection<ObjectHistoryEvent> collection = this.getCollection(oqmDbIdOrName);
		if (clientSession != null) {
			found = collection
						.find(clientSession, eq("objectId", id))
						.limit(1)
						.first();
		} else {
			found = collection
						.find(eq("objectId", id))
						.limit(1)
						.first();
		}
		
		if(found == null){
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		
		return found;
	}
	
	public ObjectHistoryEvent getLatestHistoryEventFor(String oqmDbIdOrName, ObjectId id) {
		return this.getLatestHistoryEventFor(oqmDbIdOrName, null, id);
	}
	
	@WithSpan
	public boolean hasHistoryFor(String oqmDbIdOrName, ClientSession clientSession, ObjectId id) {
		ObjectHistoryEvent found;
		MongoCollection<ObjectHistoryEvent> collection = this.getCollection(oqmDbIdOrName);
		if (clientSession != null) {
			found = collection
						.find(clientSession, eq("objectId", id))
						.limit(1)
						.first();
		} else {
			found = collection
						.find(eq("objectId", id))
						.limit(1)
						.first();
		}
		return found != null;
	}
	
	public boolean hasHistoryFor(String oqmDbIdOrName, ObjectId id) {
		return this.hasHistoryFor(oqmDbIdOrName, null, id);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, ClientSession clientSession, ObjectId id) {
		List<ObjectHistoryEvent> output = this.list(
			oqmDbIdOrName,
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
	
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, ObjectId id) {
		return this.getHistoryFor(oqmDbIdOrName, null, id);
	}
	
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, ClientSession clientSession, T object) {
		return this.getHistoryFor(oqmDbIdOrName, clientSession, object.getId());
	}
	
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, T object) {
		return this.getHistoryFor(oqmDbIdOrName, null, object);
	}
	
	@WithSpan
	public ObjectId addHistoryFor(String oqmDbIdOrName, ClientSession session, T objectReferred, InteractingEntity entity, ObjectHistoryEvent history){
		history.setObjectId(objectReferred.getId());
		if(entity != null) {
			history.setEntity(entity.getId());
		}
		ObjectId output = this.add(oqmDbIdOrName, session, history);
		this.getHens().sendEvents(this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName).getDbId(), this.clazzForObjectHistoryIsFor, history);
		return output;
	}
	public ObjectId addHistoryFor(String oqmDbIdOrName, T objectReferred, InteractingEntity entity, ObjectHistoryEvent history){
		return this.addHistoryFor(oqmDbIdOrName, null, objectReferred, entity, history);
	}
	
	@WithSpan
	public ObjectId objectCreated(String oqmDbIdOrName, ClientSession session, T created, InteractingEntity entity) {
		try {
			this.getHistoryFor(oqmDbIdOrName, session, created);
			throw new IllegalStateException(
				"History already exists for object " + this.clazzForObjectHistoryIsFor.getSimpleName() + " with id: " + created.getId()
			);
		} catch(DbNotFoundException e) {
			// no history record should exist.
		}
		
		ObjectHistoryEvent history = new CreateEvent(created, entity);
		
		return this.addHistoryFor(oqmDbIdOrName, session, created, entity, history);
	}
	
	public ObjectId objectCreated(String oqmDbIdOrName, T created, InteractingEntity interactingEntity) {
		return this.objectCreated(oqmDbIdOrName, null, created, interactingEntity);
	}
	
	@WithSpan
	public ObjectId objectUpdated(
		String oqmDbIdOrName,
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
		
		return this.addHistoryFor(oqmDbIdOrName, clientSession, updated, entity, event);
	}
	
	@Override
	public FindIterable<ObjectHistoryEvent> listIterator(String oqmDbIdOrName, ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		return super.listIterator(oqmDbIdOrName, clientSession, filter, (sort != null ? sort : Sorts.descending("timestamp")), pageOptions);
	}
	
	public ObjectId objectUpdated(String oqmDbIdOrName, T updated, InteractingEntity entity, ObjectNode updateJson, String description) {
		return this.objectUpdated(oqmDbIdOrName, null, updated, entity, updateJson, description);
	}
	
	public ObjectId objectUpdated(String oqmDbIdOrName, ClientSession clientSession, T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(oqmDbIdOrName, clientSession, updated, entity, updateJson, "");
	}
	
	public ObjectId objectUpdated(String oqmDbIdOrName, T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(oqmDbIdOrName, null, updated, entity, updateJson);
	}
	
	@WithSpan
	public ObjectId objectDeleted(String oqmDbIdOrName, ClientSession clientSession, T updated, InteractingEntity entity, String description) {
		DeleteEvent event = new DeleteEvent(updated, entity);
		
		if(description != null && !description.isBlank()){
			event.setDescription(description);
		}
		
		return this.addHistoryFor(oqmDbIdOrName, clientSession, updated, entity, event);
	}
	
	public ObjectId objectDeleted(String oqmDbIdOrName, T updated, InteractingEntity entity, String description) {
		return this.objectDeleted(oqmDbIdOrName, null, updated, entity, description);
	}
	
	public ObjectId objectDeleted(String oqmDbIdOrName, ClientSession clientSession, T updated, InteractingEntity entity) {
		return this.objectDeleted(oqmDbIdOrName, clientSession, updated, entity, "");
	}
	
	public ObjectId objectDeleted(String oqmDbIdOrName, T updated, InteractingEntity entity) {
		return this.objectDeleted(oqmDbIdOrName, null, updated, entity);
	}
	
	@Override
	public HistoryCollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, HistoryCollectionStats.builder())
				   //TODO:: this
				   .build();
	}
	
}
