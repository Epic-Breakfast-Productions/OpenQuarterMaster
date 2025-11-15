package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;

/**
 * Abstract Service that implements all basic functionality when dealing with top level mongo collections (collections that don't care about any particular OQM database)
 *
 * @param <T> The type of object this service is concerned with.
 * @param <S> The associated search object for the object
 * @param <V> The type of collection stats object to return
 */
@Slf4j
public abstract class TopLevelMongoService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> extends MongoService<T, S, V> {
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<T> collection = null;
	
	
	protected TopLevelMongoService(
		Class<T> clazz
	) {
		super(clazz);
	}
	
	/**
	 * Gets the default mongo database to use.
	 *
	 * @return The default MongoDatabase
	 */
	protected MongoDatabase getMongoDatabase() {
		return this.getMongoClient().getDatabase(this.databasePrefix);
	}
	
	/**
	 * Gets the collection name to use for this service.
	 *
	 * @return The collection name
	 */
	public String getCollectionName() {
		return getCollectionNameFromClass(this.clazz);
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
	public MongoCollection<T> getTypedCollection() {
		if (this.collection == null) {
			this.collection = this.getMongoDatabase().getCollection(this.getCollectionName(), this.clazz);
		}
		return this.collection;
	}
	
	/**
	 * Gets the raw document collection for this service.
	 * <p>
	 * You should only need to use {@link #getTypedCollection()}
	 *
	 * @return The raw document collection.
	 */
	public MongoCollection<Document> getDocumentCollection() {
		return this.getMongoDatabase().getCollection(this.getCollectionName());
	}
	
	
	private FindIterable<T> find(ClientSession session, Bson filter) {
		log.debug("Filter for find: {}", filter);
		FindIterable<T> output;
		
		MongoCollection<T> collection = this.getTypedCollection();
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
	
	public FindIterable<T> listIterator(@NonNull S searchObject) {
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
		MongoCollection<T> collection = this.getTypedCollection();
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
			pagingOptions,
			searchObject
		);
	}
	
	public CollectionStats collectionStats() {
		return CollectionStats.builder()
				   .size(this.getTypedCollection().countDocuments())
				   .build();
	}
	
	public void runPostUpgrade(ClientSession cs, CollectionUpgradeResult upgradeResult) {
		//nothing to do.
	};
}
