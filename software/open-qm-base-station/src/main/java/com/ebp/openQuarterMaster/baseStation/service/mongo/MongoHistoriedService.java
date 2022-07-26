package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.mongoUtils.exception.DbDeletedException;
import com.ebp.openQuarterMaster.baseStation.mongoUtils.exception.DbNotFoundException;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.history.ObjectHistory;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
@Traced
public abstract class MongoHistoriedService<T extends MainObject> extends MongoService<T> {
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 * TODO:: real exception
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
	
	/**
	 * TODO:: description
	 * @param id
	 * @param updateJson
	 * @param user
	 * @return
	 */
	public T update(ObjectId id, ObjectNode updateJson, User user) {
		T updated = super.update(id, updateJson, user);
		
		this.getHistoryService().updateHistoryFor(
			updated,
			user,
			updateJson
		);
		
		return updated;
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	public ObjectId add(T object, User user) {
		if(!this.allowNullUserForCreate){
			assertNotNullUser(user);
		}
		super.add(object);
		
		this.getHistoryService().createHistoryFor(
			object,
			user
		);
		
		return object.getId();
	}
	
	public ObjectId add(T object) {
		return this.add(object, null);
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
	
	public T remove(ObjectId objectId){
		throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
	}
	
	public long removeAll(User user) {
		
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
}
