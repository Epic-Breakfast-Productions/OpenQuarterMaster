package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.exception.db.DbDeleteRelationalException;
import tech.ebp.oqm.core.api.exception.db.DbDeletedException;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public abstract class MongoObjectService<T extends MainObject, S extends SearchObject<T>, X extends CollectionStats> extends MongoDbAwareService<T, S, X> {
	
	@Getter
	private Set<String> disallowedUpdateFields = new HashSet<>();
	
	protected MongoObjectService(String collectionName, Class<T> clazz) {
		super(collectionName, clazz);
	}
	
	protected MongoObjectService(Class<T> clazz) {
		super(clazz);
	}
	
	public MongoObjectService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		OqmDatabaseService oqmDatabaseService,
		String collectionName,
		Class<T> clazz
	) {
		super(objectMapper, mongoClient, database, oqmDatabaseService, collectionName, clazz);
	}
	
	/**
	 * Any data massaging needed to do just before insertion/updates.
	 * @param object
	 */
	public void massageIncomingData(String oqmDbIdOrName, @NonNull T object) {
		//nothing to do
	}
	
	
	private FindIterable<T> find(String oqmDbIdOrName, ClientSession session, Bson filter) {
		log.debug("Filter for find: {}", filter);
		FindIterable<T> output;
		
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (filter != null) {
			if (session == null) {
				output = collection.find(filter);
			} else {
				output = collection.find(session, filter);
			}
		} else {
			if (session == null) {
				output = collection.find();
			} else {
				output = collection.find(session);
			}
		}
		
		Bson sortBson = Sorts.descending("$natural");
		//TODO:: #571 support providing sort param
		
		return output.sort(sortBson);
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
	public FindIterable<T> listIterator(String oqmDbIdOrName, ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		FindIterable<T> results = this.find(oqmDbIdOrName, clientSession, filter);
		
		if (sort != null) {
			results = results.sort(sort);
		}
		if (pageOptions != null && pageOptions.isDoPaging()) {
			results = results.skip(pageOptions.getSkipVal()).limit(pageOptions.pageSize);
		}
		
		return results;
	}
	
	public FindIterable<T> listIterator(String oqmDbIdOrName, Bson filter, Bson sort, PagingOptions pageOptions) {
		return this.listIterator(oqmDbIdOrName, null, filter, sort, pageOptions);
	}
	
	public FindIterable<T> listIterator(String oqmDbIdOrName) {
		return this.listIterator(oqmDbIdOrName, null, null, null, null);
	}
	
	public FindIterable<T> listIterator(String oqmDbIdOrName, ClientSession cs) {
		return this.listIterator(oqmDbIdOrName, cs, null, null, null);
	}
	
	public FindIterable<T> listIterator(ObjectId dbId, ClientSession cs) {
		return this.listIterator(dbId.toHexString(), cs);
	}
	
	public FindIterable<T> listIterator(String oqmDbIdOrName, ClientSession cs, @NonNull S searchObject) {
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		log.debug("Filters: {}", filters);
		Bson filter = (filters.isEmpty() ? null : and(filters));
		
		return this.listIterator(
			oqmDbIdOrName,
			cs,
			filter,
			searchObject.getSortBson(),
			searchObject.getPagingOptions()
		);
	}
	
	public FindIterable<T> listIterator(String oqmDbIdOrName, @NonNull S searchObject) {
		return this.listIterator(oqmDbIdOrName, null, searchObject);
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
	public List<T> list(String oqmDbIdOrName, ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
		List<T> list = new ArrayList<>();
		this.listIterator(oqmDbIdOrName, clientSession, filter, sort, pageOptions).into(list);
		return list;
	}
	
	public List<T> list(String oqmDbIdOrName, Bson filter, Bson sort, PagingOptions pageOptions) {
		return this.list(oqmDbIdOrName, null, filter, sort, pageOptions);
	}
	
	/**
	 * Gets a list of all elements in the collection.
	 * <p>
	 * Wrapper for {@link #list(String, Bson, Bson, PagingOptions)}, with all null arguments.
	 *
	 * @return a list of all elements in the collection.
	 */
	public List<T> list(String oqmDbIdOrName, ClientSession clientSession) {
		return this.list(oqmDbIdOrName, clientSession, null, null, null);
	}
	
	public List<T> list(String oqmDbIdOrName) {
		return this.list(oqmDbIdOrName, null);
	}
	
	public Iterator<T> iterator(String oqmDbIdOrName) {
		return getTypedCollection(oqmDbIdOrName).find().iterator();
	}
	
	/**
	 * Searches the collection for objects. Uses the
	 *
	 * @param searchObject The search object to use for this object.
	 *
	 * @return The search results for the search given
	 */
	@WithSpan
	public SearchResult<T> search(String oqmDbIdOrName, ClientSession cs, @NonNull S searchObject) {
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		log.debug("Filters: {}", filters);
		Bson filter = (filters.isEmpty() ? null : and(filters));
		PagingOptions pagingOptions = searchObject.getPagingOptions();
		
		List<T> list = this.list(
			oqmDbIdOrName,
			cs,
			filter,
			searchObject.getSortBson(),
			pagingOptions
		);
		
		return new SearchResult<>(
			list,
			(int) this.count(oqmDbIdOrName, filter),
			!filters.isEmpty(),
			pagingOptions,
			searchObject
		);
	}
	
	public SearchResult<T> search(String oqmDbIdOrName, @NonNull S searchObject) {
		return this.search(oqmDbIdOrName, null, searchObject);
	}
	
	@Deprecated
	public SearchResult<T> search(String oqmDbIdOrName, @NonNull S searchObject, boolean defaultPageSizeIfNotSet) {
		return this.search(oqmDbIdOrName, searchObject);
	}
	
	/**
	 * Determines if the collection is empty or not.
	 *
	 * @return If the collection is empty or not.
	 */
	public boolean collectionEmpty(String oqmDbIdOrName) {
		return this.getTypedCollection(oqmDbIdOrName).countDocuments() == 0;
	}
	
	/**
	 * Gets the count of records in the collection using a filter.
	 *
	 * @param filter The filter to use. Nullable, gets the whole collection size if null.
	 *
	 * @return the count of records in the collection
	 */
	public long count(String oqmDbIdOrName, ClientSession clientSession, Bson filter) {
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (filter == null) {
			if (clientSession == null) {
				return collection.countDocuments();
			} else {
				return collection.countDocuments(clientSession);
			}
		}
		if (clientSession == null) {
			return collection.countDocuments(filter);
		} else {
			return collection.countDocuments(clientSession, filter);
		}
	}
	
	public long count(String oqmDbIdOrName, Bson filter) {
		return this.count(oqmDbIdOrName, null, filter);
	}
	
	/**
	 * Gets the count of all records in the collection.
	 * <p>
	 * Wrapper for {@link #count(String Bson)}.
	 *
	 * @return the count of all records in the collection.
	 */
	public long count(String oqmDbIdOrName, ClientSession clientSession) {
		if (clientSession == null) {
			return this.count(oqmDbIdOrName, (Bson) null);
		}
		return this.count(oqmDbIdOrName, clientSession, null);
	}
	
	public long count(String oqmDbIdOrName) {
		return this.count(oqmDbIdOrName, (ClientSession) null);
	}
	
	/**
	 * Gets an object with a particular id.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(String oqmDbIdOrName, ObjectId objectId) throws DbNotFoundException, DbDeletedException {
		T found = getTypedCollection(oqmDbIdOrName)
					  .find(eq("_id", objectId))
					  .limit(1)
					  .first();
		
		if (found == null) {
			throw new DbNotFoundException(this.clazz, objectId);
		}
		
		return found;
	}
	
	public T get(String oqmDbIdOrName, ClientSession clientSession, ObjectId objectId) throws DbNotFoundException, DbDeletedException {
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		T found;
		
		if (clientSession == null) {
			found = collection
						.find(eq("_id", objectId))
						.limit(1)
						.first();
		} else {
			found = collection
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
	 * Wrapper for {@link #get(String, ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(String oqmDbIdOrName, String objectId) throws DbNotFoundException, DbDeletedException {
		return this.get(oqmDbIdOrName, new ObjectId(objectId));
	}
	
	/**
	 * Updates the object at the id given. Validates the object before updating in the database.
	 *
	 * @param id The id of the object to update
	 * @param updateJson Generic JSON to describe the update. Meant to be individual fields set to the new values.
	 *
	 * @return The updated object.
	 */
	public T update(String oqmDbIdOrName, ClientSession cs, ObjectId id, ObjectNode updateJson) throws DbNotFoundException {
		if (updateJson.has("id") && !id.toHexString().equals(updateJson.get("id").asText())) {
			throw new IllegalArgumentException("Not allowed to update id of an object.");
		}
		
		T object = this.get(oqmDbIdOrName, id);
		ObjectNode origJsonObj = this.getObjectMapper().valueToTree(object);
		
		Iterator<String> updatingFields = updateJson.fieldNames();
		while (updatingFields.hasNext()) {
			String updatingField = updatingFields.next();
			if (this.getDisallowedUpdateFields().contains(updatingField)) {
				//TODO:: support sub-fields
				if (!origJsonObj.get(updatingField).equals(
					updateJson.get(updatingField)
				)) {
					throw new IllegalArgumentException("Not allowed to update field '" + updatingField + "' of an " + this.getClazz().getSimpleName());
				}
			}
		}
		
		ObjectReader reader = this.getObjectMapper()
								  .readerForUpdating(object)
								  .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
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
		this.ensureObjectValid(oqmDbIdOrName, false, object, cs);
		this.massageIncomingData(oqmDbIdOrName, object);
		
		if (cs == null) {
			this.getTypedCollection(oqmDbIdOrName).findOneAndReplace(eq("_id", id), object);
		} else {
			this.getTypedCollection(oqmDbIdOrName).findOneAndReplace(cs, eq("_id", id), object);
		}
		
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
	public T update(String oqmDbIdOrName, ClientSession cs, String id, ObjectNode updateJson) {
		return this.update(oqmDbIdOrName, cs, new ObjectId(id), updateJson);
	}
	
	public T update(String oqmDbIdOrName, ClientSession clientSession, @Valid T object) throws DbNotFoundException {
		
		this.get(oqmDbIdOrName, clientSession, object.getId());
		this.ensureObjectValid(oqmDbIdOrName, false, object, clientSession);
		this.massageIncomingData(oqmDbIdOrName, object);
		
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (clientSession != null) {
			return collection.findOneAndReplace(clientSession, eq("_id", object.getId()), object);
		} else {
			return collection.findOneAndReplace(eq("_id", object.getId()), object);
		}
	}
	
	public T update(String oqmDbIdOrName, @Valid T object) throws DbNotFoundException {
		return this.update(oqmDbIdOrName, null, object);
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	public ObjectId add(String oqmDbIdOrName, ClientSession session, @NonNull @Valid T object) {
		log.info("Adding new {}", this.getCollectionName());
		log.debug("New object: {}", object);
		
		this.ensureObjectValid(oqmDbIdOrName, true, object, session);
		this.massageIncomingData(oqmDbIdOrName, object);
		
		InsertOneResult result;
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (session == null) {
			result = collection.insertOne(object);
		} else {
			result = collection.insertOne(session, object);
		}
		
		object.setId(result.getInsertedId().asObjectId().getValue());
		
		log.info("Added. Id: {}", object.getId());
		return object.getId();
	}
	
	public ObjectId add(String oqmDbIdOrName, @NonNull @Valid T object) {
		return this.add(oqmDbIdOrName, null, object);
	}
	
	public List<ObjectId> addBulk(String oqmDbIdOrName, ClientSession clientSession, @NonNull List<@Valid @NonNull T> objects) {
		List<ObjectId> output = new ArrayList<>(objects.size());
		try (
			MongoSessionWrapper w = new MongoSessionWrapper(clientSession, this);
		) {
			w.runTransaction(()->{
				
				for (T cur : objects) {
					try {
						output.add(
							add(oqmDbIdOrName, w.getClientSession(), cur)
						);
					} catch(Throwable e) {
						w.getClientSession().abortTransaction();
						throw e;
					}
				}
				
				w.getClientSession().commitTransaction();
				return output;
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	public List<ObjectId> addBulk(String oqmDbIdOrName, @NonNull List<@Valid @NonNull T> objects) {
		return this.addBulk(oqmDbIdOrName, null, objects);
	}
	
	/**
	 * Extend this to provide validation of removal objects; checking dependencies, etc.
	 *
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession clientSession, T objectToRemove) {
		return new HashMap<>();
	}
	
	/**
	 * Asserts that the given object is not referenced by any other object.
	 *
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	protected void assertNotReferenced(String oqmDbIdOrName, ClientSession clientSession, T objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = this.getReferencingObjects(oqmDbIdOrName, clientSession, objectToRemove);
		if (!objsWithRefs.isEmpty()) {
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
	public T remove(String oqmDbIdOrName, ClientSession clientSession, ObjectId objectId) {
		T toRemove = this.get(oqmDbIdOrName, clientSession, objectId);
		
		this.assertNotReferenced(oqmDbIdOrName, clientSession, toRemove);
		
		DeleteResult result;
		MongoCollection<T> collection = this.getTypedCollection(oqmDbIdOrName);
		if (clientSession == null) {
			result = collection.deleteOne(eq("_id", objectId));
		} else {
			result = collection.deleteOne(clientSession, eq("_id", objectId));
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
	
	public T remove(String oqmDbIdOrName, ObjectId objectId) {
		return this.remove(oqmDbIdOrName, null, objectId);
	}
	
	/**
	 * Removes the object with the id given.
	 * <p>
	 * Wrapper for {@link #remove(String, ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	public T remove(String oqmDbIdOrName, String objectId) {
		return this.remove(oqmDbIdOrName, new ObjectId(objectId));
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	public long removeAll(String oqmDbIdOrName) {
		//TODO:: client session
		return this.getTypedCollection(oqmDbIdOrName).deleteMany(new BsonDocument()).getDeletedCount();
	}
	
	@Override
	public CollectionClearResult clear(String oqmDbIdOrName, @NonNull ClientSession session) {
		return CollectionClearResult.builder()
				   .collectionName(this.getCollectionName())
				   .numRecordsDeleted(this.getTypedCollection(oqmDbIdOrName).deleteMany(new BsonDocument()).getDeletedCount())
				   .build();
	}
	
	/**
	 * Gets the sum of an integer(or long) field in the object stored.
	 *
	 * @param field The field to sum
	 *
	 * @return The sum of all values at the field
	 */
	protected long getSumOfIntField(String oqmDbIdOrName, String field) {
		Document returned = this.getTypedCollection(oqmDbIdOrName).aggregate(
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
	
	protected double getSumOfFloatField(String oqmDbIdOrName, String field) {
		Document returned = this.getTypedCollection(oqmDbIdOrName).aggregate(
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
		String oqmDbIdOrName,
		String field,
		String value
	) {
		return this.getTypedCollection(oqmDbIdOrName)
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
