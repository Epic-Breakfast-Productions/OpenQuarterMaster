package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeletedException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistory;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.user.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
@Traced
public abstract class MongoHistoriedService<T extends MainObject, S extends SearchObject<T>> extends MongoService<T, S> {
	
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 * TODO:: real exception
	 *
	 * @param user
	 */
	private static void assertNotNullUser(User user) {
		if (user == null) {
			throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
		}
		//TODO:: check has id
	}
	
	protected final boolean allowNullUserForCreate;
	@Getter(AccessLevel.PROTECTED)
	private MongoHistoryService<T> historyService = null;
	
	public MongoHistoriedService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullUserForCreate,
		MongoHistoryService<T> historyService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.allowNullUserForCreate = allowNullUserForCreate;
		this.historyService = historyService;
	}
	
	protected MongoHistoriedService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz,
		boolean allowNullUserForCreate
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			clazz
		);
		this.allowNullUserForCreate = allowNullUserForCreate;
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
	public T get(ObjectId objectId) {
		try {
			return super.get(objectId);
		} catch(DbNotFoundException e) {
			try {
				ObjectHistory history = this.getHistoryService().getHistoryFor(objectId);
				//if we had history, was deleted
				//TODO:: check that last event was actually a DELETE?
				throw new DbDeletedException(e);
			} catch(DbNotFoundException e2) {
				//nothing to do. If no history, we never had it.
			}
			throw e;
		}
	}
	
	public T update(T object, HistoryEvent event) throws DbNotFoundException {
		object = super.update(object);
		this.addHistoryFor(object, event);
		return object;
	}
	
	/**
	 * TODO:: description
	 *
	 * @param id
	 * @param updateJson
	 * @param user
	 *
	 * @return
	 */
	public T update(ObjectId id, ObjectNode updateJson, User user) {
		assertNotNullUser(user);
		T updated = super.update(id, updateJson);
		
		this.getHistoryService().updateHistoryFor(
			updated,
			user,
			updateJson
		);
		
		return updated;
	}
	
	public T update(String id, ObjectNode updateJson, User user) {
		return this.update(new ObjectId(id), updateJson, user);
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	public ObjectId add(ClientSession session, @NonNull @Valid T object, User user) {
		if (!this.allowNullUserForCreate) {
			assertNotNullUser(user);
		}
		super.add(session, object);
		
		this.getHistoryService().createHistoryFor(
			session,
			object,
			user
		);
		
		return object.getId();
	}
	
	public ObjectId add(T object, User user) {
		return this.add(null, object, user);
	}
	
	public ObjectId add(@NonNull T object) {
		return this.add(object, null);
	}
	
	public List<ObjectId> addBulk(List<T> objects, User user) {
		try(
			ClientSession session = this.getMongoClient().startSession();
		){
			return session.withTransaction(()->{
				List<ObjectId> output = new ArrayList<>(objects.size());
				
				for (T cur : objects) {
					try {
						output.add(add(session, cur, user));
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
	public T remove(ObjectId objectId, User user) {
		assertNotNullUser(user);
		T removed = super.remove(objectId);
		
		this.getHistoryService().objectDeleted(
			removed,
			user
		);
		
		return removed;
	}
	
	public T remove(String objectId, User user) {
		return this.remove(new ObjectId(objectId), user);
	}
	
	public T remove(ObjectId objectId) {
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	public long removeAll(User user) {
		//TODO:: add history event to each
		return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	public long removeAll() {
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	public List<ObjectHistory> listHistory(Bson filter, Bson sort, PagingOptions pageOptions){
		return this.getHistoryService().list(filter, sort, pageOptions);
	}
	
	public Iterator<ObjectHistory> historyIterator() {
		return this.getHistoryService().iterator();
	}
	
	public SearchResult<ObjectHistory> searchHistory(HistorySearch search, boolean defaultPageSizeIfNotSet){
		return this.getHistoryService().search(search, defaultPageSizeIfNotSet);
	}
	
	
	public ObjectHistory getHistoryFor(ObjectId objectId){
		return this.getHistoryService().getHistoryFor(objectId);
	}
	
	public ObjectHistory getHistoryFor(String objectId){
		return this.getHistoryFor(new ObjectId(objectId));
	}
	
	public ObjectHistory getHistoryFor(T object){
		return this.getHistoryFor(object.getId());
	}
	
	public ObjectHistory addHistoryFor(T object, HistoryEvent event){
		return this.getHistoryService().addHistoryEvent(object.getId(), event);
	}
	
	//TODO:: more aggregate history functions (counts updated since, etc)?
	
}
