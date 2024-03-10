package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeleteRelationalException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeletedException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public abstract class MongoObjectService<T extends MainObject, S extends SearchObject<T>, X extends CollectionStats> extends MongoService<T, S, X> {
	
	public MongoObjectService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
	}
	
	protected MongoObjectService(ObjectMapper objectMapper, MongoClient mongoClient, String database, Class<T> clazz) {
		super(objectMapper, mongoClient, database, clazz);
	}
	
	private FindIterable<T> find(ClientSession session, Bson filter) {
		log.debug("Filter for find: {}", filter);
		if (filter != null) {
			if (session == null) {
				return getCollection().find(filter);
			}
			return getCollection().find(session, filter);
		}
		if (session == null) {
			return getCollection().find();
		}
		return getCollection().find(session);
	}
	
	/**
	 * Gets an iterator of entries based on the options given.
	 * <p>
	 *     TODO:: deal with client session
	 * TODO:: look into better, faster paging methods: https://dzone.com/articles/fast-paging-with-mongodb
	 *
	 * @param filter The filter to use for the search. Nullable, no filter if null.
	 * @param sort The bson used to describe the sorting behavior. Nullable, no explicit sorting if null.
	 * @param pageOptions The paging options. Nullable, not used if null.
	 *
	 * @return a list of entries based on the options given.
	 */
	public FindIterable<T> listIterator(ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		FindIterable<T> results = this.find(clientSession, filter);
		
		if (sort != null) {
			results = results.sort(sort);
		}
		if (pageOptions != null && pageOptions.isDoPaging()) {
			results = results.skip(pageOptions.getSkipVal()).limit(pageOptions.pageSize);
		}
		
		return results;
	}
	
	public FindIterable<T> listIterator(Bson filter, Bson sort, PagingOptions pageOptions) {
		return this.listIterator(null, filter, sort, pageOptions);
	}
	
	public FindIterable<T> listIterator() {
		return this.listIterator(null, null, null, null);
	}
	public FindIterable<T> listIterator(ClientSession cs) {
		return this.listIterator(cs, null, null, null);
	}
	
	/**
	 * Gets a list of entries based on the options given.
	 *
	 * @param filter The filter to use for the search. Nullable, no filter if null.
	 * @param sort The bson used to describe the sorting behavior. Nullable, no explicit sorting if null.
	 * @param pageOptions The paging options. Nullable, not used if null.
	 *
	 * @return a list of entries based on the options given.
	 */
	public List<T> list(ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		List<T> list = new ArrayList<>();
		this.listIterator(clientSession, filter, sort, pageOptions).into(list);
		return list;
	}
	
	public List<T> list(Bson filter, Bson sort, PagingOptions pageOptions) {
		return this.list(null, filter, sort, pageOptions);
	}
	
	/**
	 * Gets a list of all elements in the collection.
	 * <p>
	 * Wrapper for {@link #list(Bson, Bson, PagingOptions)}, with all null arguments.
	 *
	 * @return a list of all elements in the collection.
	 */
	public List<T> list(ClientSession clientSession) {
		return this.list(clientSession, null, null, null);
	}
	
	public List<T> list() {
		return this.list(null);
	}
	
	public Iterator<T> iterator() {
		return getCollection().find().iterator();
	}
	
	/**
	 * Searches the collection for objects. Uses the
	 *
	 * @param searchObject The search object to use for this object.
	 *
	 * @return The search results for the search given
	 */
	@WithSpan
	public SearchResult<T> search(@NonNull S searchObject, boolean defaultPageSizeIfNotSet) {
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		log.debug("Filters: {}", filters);
		Bson filter = (filters.isEmpty() ? null : and(filters));
		PagingOptions pagingOptions = searchObject.getPagingOptions();
		
		List<T> list = this.list(
			filter,
			searchObject.getSortBson(),
			pagingOptions
		);
		
		return new SearchResult<>(
			list,
			this.count(filter),
			!filters.isEmpty(),
			pagingOptions
		);
	}
	
	/**
	 * Determines if the collection is empty or not.
	 *
	 * @return If the collection is empty or not.
	 */
	public boolean collectionEmpty() {
		return this.getCollection().countDocuments() == 0;
	}
	
	/**
	 * Gets the count of records in the collection using a filter.
	 *
	 * @param filter The filter to use. Nullable, gets the whole collection size if null.
	 *
	 * @return the count of records in the collection
	 */
	public long count(ClientSession clientSession, Bson filter) {
		if (filter == null) {
			if (clientSession == null) {
				return getCollection().countDocuments();
			} else {
				return getCollection().countDocuments(clientSession);
			}
		}
		if (clientSession == null) {
			return getCollection().countDocuments(filter);
		} else {
			return getCollection().countDocuments(clientSession, filter);
		}
	}
	
	public long count(Bson filter) {
		return this.count(null, filter);
	}
	
	/**
	 * Gets the count of all records in the collection.
	 * <p>
	 * Wrapper for {@link #count(Bson)}.
	 *
	 * @return the count of all records in the collection.
	 */
	public long count(ClientSession clientSession) {
		if (clientSession == null) {
			return this.count((Bson) null);
		}
		return this.count(clientSession, null);
	}
	
	public long count() {
		return this.count((ClientSession) null);
	}
	
	/**
	 * Gets an object with a particular id.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(ObjectId objectId) throws DbNotFoundException, DbDeletedException {
		T found = getCollection()
					  .find(eq("_id", objectId))
					  .limit(1)
					  .first();
		
		if (found == null) {
			throw new DbNotFoundException(this.clazz, objectId);
		}
		
		return found;
	}
	
	public T get(ClientSession clientSession, ObjectId objectId) throws DbNotFoundException, DbDeletedException {
		T found;
		
		if (clientSession == null) {
			found = getCollection()
						.find(eq("_id", objectId))
						.limit(1)
						.first();
		} else {
			found = getCollection()
						.find(clientSession, eq("_id", objectId))
						.limit(1)
						.first();
		}
		
		if (found == null) {
			throw new DbNotFoundException(this.clazz, objectId);
		}
		
		return found;
	}
	
	/**
	 * Gets an object with a particular id.
	 * <p>
	 * Wrapper for {@link #get(ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(String objectId) throws DbNotFoundException, DbDeletedException {
		return this.get(new ObjectId(objectId));
	}
	
	/**
	 * Updates the object at the id given. Validates the object before updating in the database.
	 *
	 * @param id The id of the object to update
	 * @param updateJson Generic JSON to describe the update. Meant to be individual fields set to the new values.
	 *
	 * @return The updated object.
	 */
	public T update(ObjectId id, ObjectNode updateJson) throws DbNotFoundException, DbDeletedException {
		if (updateJson.has("id") && !id.toHexString().equals(updateJson.get("id").asText())) {
			throw new IllegalArgumentException("Not allowed to update id of an object.");
		}
		
		T object = this.get(id);
		
		ObjectReader reader = this.getObjectMapper()
								  .readerForUpdating(object)
								  .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			//TODO:: enable different types, for InventoryItem (fails to deal with the abstract type)
			reader.readValue(updateJson, object.getClass());
		} catch(IOException e) {
			throw new IllegalArgumentException("Unable to update with data given: " + e.getMessage(), e);
		}
		
		Set<ConstraintViolation<T>> validationErrs = VALIDATOR.validate(object);
		if (!validationErrs.isEmpty()) {
			throw new IllegalArgumentException("Unable to update with data given. Resulting object is invalid: " +
											   validationErrs.stream()
												   .map(ConstraintViolation::getMessage)
												   .collect(Collectors.joining(", ")));
		}
		this.ensureObjectValid(false, object, null);//TODO:: add client session
		
		this.getCollection().findOneAndReplace(eq("_id", id), object);
		return object;
	}
	
	/**
	 * Updates the object at the id given. Validates the object before updating in the database.
	 *
	 * @param id The id of the object to update
	 * @param updateJson Generic JSON to describe the update. Meant to be individual fields set to the new values.
	 *
	 * @return The updated object.
	 */
	public T update(String id, ObjectNode updateJson) {
		return this.update(new ObjectId(id), updateJson);
	}
	
	public T update(ClientSession clientSession, @Valid T object) throws DbNotFoundException {
		//TODO:: review this
		this.get(clientSession, object.getId());
		if (clientSession != null) {
			return this.getCollection().findOneAndReplace(clientSession, eq("_id", object.getId()), object);
		} else {
			return this.getCollection().findOneAndReplace(eq("_id", object.getId()), object);
		}
	}
	
	public T update(@Valid T object) throws DbNotFoundException {
		return this.update(null, object);
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	public ObjectId add(ClientSession session, @NonNull @Valid T object) {
		log.info("Adding new {}", this.getCollectionName());
		log.debug("New object: {}", object);
		
		this.ensureObjectValid(true, object, session);
		
		InsertOneResult result;
		if (session == null) {
			result = getCollection().insertOne(object);
		} else {
			result = getCollection().insertOne(session, object);
		}
		
		object.setId(result.getInsertedId().asObjectId().getValue());
		
		log.info("Added. Id: {}", object.getId());
		return object.getId();
	}
	
	public ObjectId add(@NonNull @Valid T object) {
		return this.add(null, object);
	}
	
	public List<ObjectId> addBulk(@NonNull List<@Valid @NonNull T> objects) {
		try (
			ClientSession session = this.getNewClientSession();
		) {
			return session.withTransaction(()->{
				List<ObjectId> output = new ArrayList<>(objects.size());
				
				for (T cur : objects) {
					try {
						output.add(add(session, cur));
					} catch(Throwable e) {
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
	 * Extend this to provide validation of removal objects; checking dependencies, etc.
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	public Map<String, Set<ObjectId>> getReferencingObjects(ClientSession clientSession, T objectToRemove){
		return new HashMap<>();
	}
	
	/**
	 * Asserts that the given object is not referenced by any other object.
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	protected void assertNotReferenced(ClientSession clientSession, T objectToRemove){
		Map<String, Set<ObjectId>> objsWithRefs = this.getReferencingObjects(clientSession, objectToRemove);
		if(!objsWithRefs.isEmpty()){
			throw new DbDeleteRelationalException(objectToRemove, objsWithRefs);
		}
	}
	
	/**
	 * Removes the object with the id given.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	public T remove(ClientSession clientSession, ObjectId objectId) {
		T toRemove = this.get(clientSession, objectId);
		
		this.assertNotReferenced(clientSession, toRemove);
		
		DeleteResult result;
		if (clientSession == null) {
			result = this.getCollection().deleteOne(eq("_id", objectId));
		} else {
			result = this.getCollection().deleteOne(clientSession, eq("_id", objectId));
		}
		
		{//TODO: ignore this in coverage
			if (!result.wasAcknowledged()) {
				log.warn("Delete of obj {} was not acknowledged.", objectId);
			}
			if (result.getDeletedCount() != 1) {
				log.warn("Delete of obj {} returned delete count != 1: {}", objectId, result.getDeletedCount());
			}
		}
		
		return toRemove;
	}
	
	public T remove(ObjectId objectId) {
		return this.remove(null, objectId);
	}
	
	/**
	 * Removes the object with the id given.
	 * <p>
	 * Wrapper for {@link #remove(ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	public T remove(String objectId) {
		return this.remove(new ObjectId(objectId));
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	public long removeAll() {
		//TODO:: client session
		return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
	}
	
	/**
	 * Gets the sum of an integer(or long) field in the object stored.
	 *
	 * @param field The field to sum
	 *
	 * @return The sum of all values at the field
	 */
	protected long getSumOfIntField(String field) {
		Document returned = this.getCollection().aggregate(
			List.of(
				new Document(
					"$group",
					new Document("_id", new BsonNull())
						.append("value", new Document("$sum", "$" + field))
				)),
			Document.class
		).first();
		
		if (returned == null) {
			return 0;
		}
		
		return returned.get("value", Number.class).longValue();
	}
	
	protected double getSumOfFloatField(String field) {
		Document returned = this.getCollection().aggregate(
			List.of(
				new Document(
					"$group",
					new Document("_id", new BsonNull())
						.append("value", new Document("$sum", "$" + field))
				)),
			Document.class
		).first();
		
		if (returned == null) {
			return 0.0;
		}
		
		return returned.get("value", Number.class).doubleValue();
	}
	
	public boolean fieldValueExists(
		String field,
		String value
	) {
		return this.getCollection()
				   .find(
					   eq(field, value)
				   ).limit(1)
				   .first() != null;
	}
	
	//TODO
	//	protected BigInteger getSumOfBigIntField(String field){
	//		Document returned = this.getCollection().aggregate(
	//			List.of(
	//				new Document(
	//					"$group",
	//					new Document("_id", new BsonNull())
	//						.append("value", new Document("$sum", "$"+field))
	//				)),
	//			Document.class
	//		).first();
	//
	//		return returned.get("value", Number.class).doubleValue();
	//	}
}