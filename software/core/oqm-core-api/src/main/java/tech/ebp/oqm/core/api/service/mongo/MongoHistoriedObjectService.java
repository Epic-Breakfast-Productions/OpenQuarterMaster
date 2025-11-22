package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.FieldsAffectedHistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.management.HistoriedCollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.exception.db.DbDeletedException;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoHistoriedObjectService<T extends MainObject, S extends SearchObject<T>, X extends CollectionStats> extends MongoObjectService<T, S, X> {
	
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 * TODO:: real exception
	 *
	 * @param interactingEntity
	 */
	private static void assertNotNullEntity(InteractingEntity interactingEntity) {
		if (interactingEntity == null) {
			throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
		}
		//TODO:: check has id
	}

	public static Map<String, HistoryDetail> detailListToMap(HistoryDetail ... details){
		return Arrays.stream(details).collect(Collectors.toMap((detail)->detail.getType().name(), Function.identity()));
	}
	
	@Getter
	protected final boolean allowNullEntityForCreate;
	@Getter
	private MongoHistoryService<T> historyService = null;
	
	protected MongoHistoriedObjectService(
		String collectionName,
		Class<T> clazz,
		boolean allowNullEntityForCreate
	) {
		super(collectionName, clazz);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
	}
	
	protected MongoHistoriedObjectService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		OqmDatabaseService oqmDatabaseService,
		String collectionName,
		Class<T> clazz,
		boolean allowNullEntityForCreate
	) {
		super(objectMapper, mongoClient, database, oqmDatabaseService, collectionName, clazz);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
	}
	
	protected MongoHistoriedObjectService(
		Class<T> clazz,
		boolean allowNullEntityForCreate
	) {
		this(
			getCollectionNameFromClass(clazz),
			clazz,
			allowNullEntityForCreate
		);
	}
	
	@PostConstruct
	public void setup() {
		this.historyService = new MongoHistoryService<>(
			this.getObjectMapper(),
			this.getMongoClient(),
			this.getDatabasePrefix(),
			this.getOqmDatabaseService(),
			clazz
		);
	}
	
	/**
	 * Gets an object with a particular id.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	@Override
	@WithSpan
	public T get(String oqmDbIdOrName, ObjectId objectId) {
		try {
			return super.get(oqmDbIdOrName, objectId);
		} catch(DbNotFoundException e) {
			try {
				DeleteEvent deletedEvent = this.getHistoryService().isDeleted(oqmDbIdOrName, objectId);
				
				throw new DbDeletedException(this.clazz, deletedEvent);
			} catch(DbDeletedException e2) {
				//nothing to do. If no history, we never had it.
			}
			throw e;
		}
	}

	@WithSpan
	public T update(String oqmDbIdOrName, ClientSession cs, T object, InteractingEntity entity, HistoryDetail ... details) throws DbNotFoundException {
		object = this.update(oqmDbIdOrName, cs, object);
		this.addHistoryFor(oqmDbIdOrName, cs, object, entity,
			UpdateEvent.builder()
				.objectId(object.getId())
				.entity(entity.getId())
				.details(detailListToMap(details))
				.build()
		);
		return object;
	}
	
	@WithSpan
	public T update(String oqmDbIdOrName, T object, InteractingEntity entity, HistoryDetail ... details) throws DbNotFoundException {
		return this.update(oqmDbIdOrName, null, object, entity, details);
	}
	
	/**
	 * TODO:: description
	 *
	 * @param id
	 * @param updateJson
	 * @param interactingEntity
	 *
	 * @return
	 */
	@WithSpan
	public T update(String oqmDbIdOrName, ClientSession cs, ObjectId id, ObjectNode updateJson, InteractingEntity interactingEntity, HistoryDetail ... details) {
		assertNotNullEntity(interactingEntity);
		T updated = this.update(oqmDbIdOrName, cs, id, updateJson);

		List<HistoryDetail> newDeets = new ArrayList<>(List.of(details));
		newDeets.add(FieldsAffectedHistoryDetail.builder().fieldsUpdated(FieldsAffectedHistoryDetail.getFields(updateJson)).build());
		
		this.getHistoryService().objectUpdated(
			oqmDbIdOrName,
			cs,
			updated,
			interactingEntity,
			newDeets.toArray(HistoryDetail[]::new)
		);
		
		return updated;
	}

	public T update(String oqmDbIdOrName, ClientSession cs, String id, ObjectNode updateJson, InteractingEntity interactingEntity, HistoryDetail ... details) {
		return this.update(oqmDbIdOrName, cs, new ObjectId(id), updateJson, interactingEntity, details);
	}

		/**
		 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
		 *
		 * @param object The object to add
		 *
		 * @return The id of the newly added object.
		 */
	@WithSpan
	public ObjectId add(String oqmDbIdOrName, ClientSession session, @NonNull @Valid T object, InteractingEntity entity, HistoryDetail ... details) {
		if (!this.allowNullEntityForCreate) {
			assertNotNullEntity(entity);
		}
		super.add(oqmDbIdOrName, session, object);
		
		this.getHistoryService().objectCreated(
			oqmDbIdOrName,
			session,
			object,
			entity,
			details
		);
		
		return object.getId();
	}

	public ObjectId add(ObjectId oqmDbIdOrName, ClientSession session, @NonNull @Valid T object, InteractingEntity entity) {
		return this.add(oqmDbIdOrName.toHexString(), session, object, entity);
	}
	
	public ObjectId add(String oqmDbIdOrName, T object, InteractingEntity interactingEntity) {
		return this.add(oqmDbIdOrName, null, object, interactingEntity);
	}
	
	public ObjectId add(String oqmDbIdOrName, @NonNull T object) {
		//TODO:: tweak see if this works/ passes tests/ test manually
		//		if (!this.allowNullEntityForCreate) {
		//			assertNotNullEntity(entity);
		//		}
		return this.add(oqmDbIdOrName, object, null);
	}
	
	
	@WithSpan
	public List<ObjectId> addBulk(String oqmDbIdOrName, ClientSession session, @NonNull @Valid List<T> objects, InteractingEntity entity, HistoryDetail ... details) {
		if (!this.allowNullEntityForCreate) {
			assertNotNullEntity(entity);
		}
		
		List<ObjectId> output;
		try(
			MongoSessionWrapper sessionWrapper = new MongoSessionWrapper(session, this)
			){
			output = sessionWrapper.runTransaction(()->{
				List<ObjectId> ids = super.addBulk(oqmDbIdOrName, sessionWrapper.getClientSession(), objects);
				
				for(T object : objects) {
					this.getHistoryService().objectCreated(
						oqmDbIdOrName,
						session,
						object,
						entity,
						details
					);
				}
				return ids;
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return output;
	}
	
	@WithSpan
	public List<ObjectId> addBulk(String oqmDbIdOrName, List<T> objects, InteractingEntity entity) {
		return this.addBulk(oqmDbIdOrName, null, objects, entity);
	}
	
	/**
	 * Removes the object with the id given.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	@WithSpan
	public T remove(String oqmDbIdOrName, ClientSession session, ObjectId objectId, InteractingEntity entity, HistoryDetail ... details) {
		assertNotNullEntity(entity);
		T removed = super.remove(oqmDbIdOrName, session, objectId);
		
		this.getHistoryService().objectDeleted(
			oqmDbIdOrName,
			session,
			removed,
			entity,
			details
		);
		
		return removed;
	}
	
	public T remove(String oqmDbIdOrName, ObjectId objectId, InteractingEntity entity) {
		return this.remove(oqmDbIdOrName, null, objectId, entity);
	}
	
	public T remove(String oqmDbIdOrName, String objectId, InteractingEntity entity) {
		return this.remove(oqmDbIdOrName, new ObjectId(objectId), entity);
	}
	
	@Override
	public T remove(String oqmDbIdOrName, ObjectId objectId) {
		//TODO:: throw better
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	@WithSpan
	public long removeAll(String oqmDbIdOrName, ClientSession session, InteractingEntity entity) {
		//TODO:: add history event to each
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (session == null) {
			return collection.deleteMany(new BsonDocument()).getDeletedCount();
		} else {
			return collection.deleteMany(session, new BsonDocument()).getDeletedCount();
		}
	}
	
	@WithSpan
	public long removeAll(String oqmDbIdOrName, InteractingEntity entity) {
		return this.removeAll(oqmDbIdOrName, null, entity);
	}
	
	@Override
	public HistoriedCollectionClearResult clear(String oqmDbIdOrName, @NonNull ClientSession session) {
		CollectionClearResult historyClearResult = this.getHistoryService().clear(oqmDbIdOrName, session);
		CollectionClearResult superClearResult = super.clear(oqmDbIdOrName, session);

		return HistoriedCollectionClearResult.builder()
			.collectionName(superClearResult.getCollectionName())
			.numRecordsDeleted(superClearResult.getNumRecordsDeleted())
			.historyCollectionResult(historyClearResult)
			.build();
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	@Override
	public long removeAll(String oqmDbIdOrName) {
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> listHistory(String oqmDbIdOrName, Bson filter, Bson sort, PagingOptions pageOptions) {
		return this.getHistoryService().list(oqmDbIdOrName, filter, sort, pageOptions);
	}
	
	@WithSpan
	public Iterator<ObjectHistoryEvent> historyIterator(String oqmDbIdOrName) {
		return this.getHistoryService().iterator(oqmDbIdOrName);
	}
	
	@WithSpan
	public SearchResult<ObjectHistoryEvent> searchHistory(String oqmDbIdOrName, HistorySearch search, boolean defaultPageSizeIfNotSet) {
		return this.getHistoryService().search(oqmDbIdOrName, search, defaultPageSizeIfNotSet);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, ObjectId objectId) {
		return this.getHistoryService().getHistoryFor(oqmDbIdOrName, objectId);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, String objectId) {
		return this.getHistoryFor(oqmDbIdOrName, new ObjectId(objectId));
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(String oqmDbIdOrName, T object) {
		return this.getHistoryFor(oqmDbIdOrName, object.getId());
	}

	@WithSpan
	public ObjectId addHistoryFor(String oqmDbIdOrName, ClientSession clientSession, ObjectId object, InteractingEntity entity, ObjectHistoryEvent event) {
		return this.getHistoryService().addHistoryFor(oqmDbIdOrName, clientSession, object, entity, event);
	}

	@WithSpan
	public ObjectId addHistoryFor(String oqmDbIdOrName, ClientSession clientSession, T object, InteractingEntity entity, ObjectHistoryEvent event) {
		return this.getHistoryService().addHistoryFor(oqmDbIdOrName, clientSession, object, entity, event);
	}
	
	@WithSpan
	public ObjectId addHistoryFor(String oqmDbIdOrName, T object, InteractingEntity entity, ObjectHistoryEvent event) {
		return this.addHistoryFor(oqmDbIdOrName, null, object, entity, event);
	}
	
	@WithSpan
	public CreateEvent getCreateEvent(String oqmDbIdOrName, ObjectId objectId) {
		CreateEvent output = (CreateEvent) this.getHistoryService().listIterator(
				oqmDbIdOrName,
				Filters.and(
					Filters.eq("type", EventType.CREATE),
					Filters.eq("objectId", objectId)
				),
				null,
				null
			)
											   .limit(1)
											   .first();
		
		//TODO:: validate; if null, exception
		ObjectId reference = output.getEntity();
		return output;
	}
	
	//TODO:: more aggregate history functions (counts updated since, etc)?
	
}
