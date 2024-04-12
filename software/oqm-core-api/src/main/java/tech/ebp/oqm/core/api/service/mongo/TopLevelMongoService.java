package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class TopLevelMongoService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> extends MongoService<T, S, V> {
	
	//TODO:: move to constructor?
	protected static final Validator VALIDATOR;
	
	static {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			VALIDATOR = validatorFactory.getValidator();
		}
	}
	
	/**
	 * Mapper to help deal with json updates.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	ObjectMapper objectMapper;
	
	/**
	 * The name of the database to access
	 */
	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String databasePrefix;
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<T> collection = null;
	
	
	protected TopLevelMongoService(
		Class<T> clazz
	){
		super(clazz);
	}
	
	/**
	 * TODO::
	 * @return
	 */
	protected MongoDatabase getMongoDatabase(){
		return this.getMongoClient().getDatabase(this.databasePrefix);
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
	protected MongoCollection<T> getCollection() {
		if (this.collection == null) {
			this.collection = this.getMongoDatabase().getCollection(getCollectionNameFromClass(this.clazz), this.clazz);
		}
		return this.collection;
	}
	
	protected MongoCollection<T> getCollection(OqmMongoDatabase db) {
		//TODO
		return null;
	}
	
	public static TransactionOptions getDefaultTransactionOptions() {
		return TransactionOptions.builder()
								 .readPreference(ReadPreference.primary())
								 .readConcern(ReadConcern.LOCAL)
								 .writeConcern(WriteConcern.MAJORITY)
								 .build();
	}
	
	@WithSpan
	public ClientSession getNewClientSession(boolean startTransaction) {
		ClientSession clientSession = this.getMongoClient().startSession();
		
		if(startTransaction){
			clientSession.startTransaction();
		}
		
		return clientSession;
	}
	
	public ClientSession getNewClientSession() {
		return this.getNewClientSession(false);
	}
	
	private FindIterable<T> find(ClientSession session, Bson filter) {
		log.debug("Filter for find: {}", filter);
		FindIterable<T> output;
		
		MongoCollection<T> collection = this.getCollection();
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
	
	public FindIterable<T> listIterator(String oqmDbIdOrName, @NonNull S searchObject) {
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		log.debug("Filters: {}", filters);
		Bson filter = (filters.isEmpty() ? null : and(filters));
		
		return this.listIterator(
			filter,
			searchObject.getSortBson(),
			searchObject.getPagingOptions()
		);
	}
	
	public long count(ClientSession clientSession, Bson filter) {
		MongoCollection<T> collection = this.getCollection();
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
	
	public long count(Bson filter) {
		return this.count(null, filter);
	}
	
	@WithSpan
	public SearchResult<T> search(@NonNull S searchObject) {
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		log.debug("Filters: {}", filters);
		Bson filter = (filters.isEmpty() ? null : and(filters));
		PagingOptions pagingOptions = searchObject.getPagingOptions();
		
		List<T> list = this.listIterator(
			filter,
			searchObject.getSortBson(),
			pagingOptions
		).into(new ArrayList<>());
		
		return new SearchResult<>(
			list,
			this.count(filter),
			!filters.isEmpty(),
			pagingOptions
		);
	}
	
//	public FindIterable<T> listIterator(String oqmDbIdOrName, @NonNull S searchObject) {
//		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
//
//		List<Bson> filters = searchObject.getSearchFilters();
//		log.debug("Filters: {}", filters);
//		Bson filter = (filters.isEmpty() ? null : and(filters));
//
//		return this.listIterator(
//			oqmDbIdOrName,
//			filter,
//			searchObject.getSortBson(),
//			searchObject.getPagingOptions()
//		);
//	}
	
	public CollectionStats collectionStats() {
		return CollectionStats.builder()
				   .size(this.getCollection().countDocuments())
				   .build();
	}
}
