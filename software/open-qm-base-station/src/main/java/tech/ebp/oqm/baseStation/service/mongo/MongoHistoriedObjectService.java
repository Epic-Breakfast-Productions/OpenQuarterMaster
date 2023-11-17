package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.CreateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeletedException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoHistoriedObjectService<T extends MainObject, S extends SearchObject<T>> extends MongoObjectService<T, S> {
	
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
	
	@Getter
	protected final boolean allowNullEntityForCreate;
	@Getter
	private MongoHistoryService<T> historyService = null;
	
	public MongoHistoriedObjectService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoryService<T> historyService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.historyService = historyService;
	}
	
	protected MongoHistoriedObjectService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz,
		boolean allowNullEntityForCreate
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			clazz
		);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.historyService = new MongoHistoryService<>(
			objectMapper,
			mongoClient,
			database,
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
	@WithSpan
	public T get(ObjectId objectId) {
		try {
			return super.get(objectId);
		} catch(DbNotFoundException e) {
			try {
				DeleteEvent deletedEvent = this.getHistoryService().isDeleted(objectId);
				
				throw new DbDeletedException(this.clazz, deletedEvent);
			} catch(DbDeletedException e2) {
				//nothing to do. If no history, we never had it.
			}
			throw e;
		}
	}
	
	public T update(ClientSession cs, T object, InteractingEntity entity, ObjectHistoryEvent event) throws DbNotFoundException {
		object = this.update(cs, object);
		this.addHistoryFor(cs, object, entity, event);
		return object;
	}
	
	@WithSpan
	public T update(T object, InteractingEntity entity, ObjectHistoryEvent event) throws DbNotFoundException {
		return this.update(null, object, entity, event);
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
	public T update(ObjectId id, ObjectNode updateJson, InteractingEntity interactingEntity) {
		assertNotNullEntity(interactingEntity);
		T updated = this.update(id, updateJson);
		
		this.getHistoryService().objectUpdated(
			updated,
			interactingEntity,
			updateJson
		);
		
		return updated;
	}
	
	public T update(String id, ObjectNode updateJson, InteractingEntity interactingEntity) {
		return this.update(new ObjectId(id), updateJson, interactingEntity);
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	@WithSpan
	public ObjectId add(ClientSession session, @NonNull @Valid T object, InteractingEntity entity) {
		if (!this.allowNullEntityForCreate) {
			assertNotNullEntity(entity);
		}
		super.add(session, object);
		
		this.getHistoryService().objectCreated(
			session,
			object,
			entity
		);
		
		return object.getId();
	}
	
	public ObjectId add(T object, InteractingEntity interactingEntity) {
		return this.add(null, object, interactingEntity);
	}
	
	public ObjectId add(@NonNull T object) {
		//TODO:: tweak see if this works/ passes tests/ test manually
//		if (!this.allowNullEntityForCreate) {
//			assertNotNullEntity(entity);
//		}
		return this.add(object, null);
	}
	
	@WithSpan
	public List<ObjectId> addBulk(List<T> objects, InteractingEntity entity) {
		try(
			ClientSession session = this.getMongoClient().startSession();
		){
			return session.withTransaction(()->{
				List<ObjectId> output = new ArrayList<>(objects.size());
				
				for (T cur : objects) {
					try {
						output.add(add(session, cur, entity));
					} catch(Throwable e){
						session.abortTransaction();
						throw e;
					}
				}
				
				session.commitTransaction();
				return output;
			}, this.getDefaultTransactionOptions());
		}
	}
	
	/**
	 * Removes the object with the id given.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	@WithSpan
	public T remove(ClientSession session, ObjectId objectId, InteractingEntity entity) {
		assertNotNullEntity(entity);
		T removed = super.remove(session, objectId);
		
		this.getHistoryService().objectDeleted(
			session,
			removed,
			entity
		);
		
		return removed;
	}
	
	public T remove(ObjectId objectId, InteractingEntity entity) {
		return this.remove(null, objectId, entity);
	}
	
	public T remove(String objectId, InteractingEntity entity) {
		return this.remove(new ObjectId(objectId), entity);
	}
	
	public T remove(ObjectId objectId) {
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	@WithSpan
	public long removeAll(ClientSession session, InteractingEntity entity) {
		//TODO:: add history event to each
		if(session == null) {
			return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
		} else {
			return this.getCollection().deleteMany(session, new BsonDocument()).getDeletedCount();
		}
	}
	
	@WithSpan
	public long removeAll(InteractingEntity entity) {
		return this.removeAll(null, entity);
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	public long removeAll() {
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> listHistory(Bson filter, Bson sort, PagingOptions pageOptions){
		return this.getHistoryService().list(filter, sort, pageOptions);
	}
	
	@WithSpan
	public Iterator<ObjectHistoryEvent> historyIterator() {
		return this.getHistoryService().iterator();
	}
	
	@WithSpan
	public SearchResult<ObjectHistoryEvent> searchHistory(HistorySearch search, boolean defaultPageSizeIfNotSet){
		return this.getHistoryService().search(search, defaultPageSizeIfNotSet);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(ObjectId objectId){
		return this.getHistoryService().getHistoryFor(objectId);
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(String objectId){
		return this.getHistoryFor(new ObjectId(objectId));
	}
	
	@WithSpan
	public List<ObjectHistoryEvent> getHistoryFor(T object){
		return this.getHistoryFor(object.getId());
	}
	
	@WithSpan
	public ObjectId addHistoryFor(ClientSession clientSession, T object, InteractingEntity entity, ObjectHistoryEvent event){
		return this.getHistoryService().addHistoryFor(clientSession, object, entity, event);
	}
	
	@WithSpan
	public ObjectId addHistoryFor(T object, InteractingEntity entity, ObjectHistoryEvent event) {
		return this.addHistoryFor(null, object, entity, event);
	}
	
	
	
	@WithSpan
	public CreateEvent getCreateEvent(ObjectId objectId){
		CreateEvent output = (CreateEvent) this.getHistoryService().listIterator(
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
