package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.inject.Inject;
import jakarta.validation.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to note a mongo service that is aware of OQM databases.
 * @param <T> The type of object this service is concerned with
 * @param <S>
 * @param <V>
 */
@Slf4j
public abstract class MongoDbAwareService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> extends MongoService<T, S, V> {


	@Getter
	@Inject
	OqmDatabaseService oqmDatabaseService;

	@Getter
	@Inject
	ImageService imageService;

	@Getter
	@Inject
	FileAttachmentService fileAttachmentService;
	
	/**
	 * The name of the collection this service is in charge of
	 */
	@Getter
	protected final String collectionName;


	private Map<ObjectId, MongoCollection<T>> collections = new HashMap<>();
	
	
	protected MongoDbAwareService(
		String collectionName,
		Class<T> clazz
	){
		super(clazz);
		this.collectionName = collectionName;
	}
	
	protected MongoDbAwareService(Class<T> clazz){
		this(getCollectionNameFromClass(clazz), clazz);
	}
	
	protected MongoDbAwareService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String databasePrefix,
		OqmDatabaseService oqmDatabaseService,
		String collectionName,
		Class<T> clazz
	) {
		this(collectionName, clazz);
		this.objectMapper = objectMapper;
		this.mongoClient = mongoClient;
		this.databasePrefix = databasePrefix;
		this.oqmDatabaseService = oqmDatabaseService;
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
//	protected MongoCollection<T> getCollection() {
//		if (this.collection == null) {
//			this.collection = this.getMongoDatabase().getCollection(this.collectionName, this.clazz);
//		}
//		return this.collection;
//	}
	
	protected MongoCollection<T> getTypedCollection(DbCacheEntry db) {
		log.trace("Getting collection for cache entry {}", db);
		if(!this.collections.containsKey(db.getDbId())){
			log.info("Collection for db cache entry not present. Creating. Cache entry: {}", db);
			this.collections.put(
				db.getDbId(),
				db.getMongoDatabase().getCollection(this.collectionName, this.clazz)
			);
		}
		
		MongoCollection<T> output =  this.collections.get(db.getDbId());
		
		if(output == null){
			log.warn("Collection gotten was null. This is an error. DB Cache entry: {}", db);
		}
		
		return output;
	}

	public MongoCollection<T> getTypedCollection(String oqmDbIdOrName) {
		DbCacheEntry dbCacheEntry = this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName);

		return this.getTypedCollection(dbCacheEntry);
	}

	public MongoCollection<Document> getDocumentCollection(String oqmDbIdOrName) {
		DbCacheEntry dbCacheEntry = this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName);
		return dbCacheEntry.getMongoDatabase().getCollection(this.collectionName);
	}
	
	/**
	 * Method to check that an object is [still] valid before applying creation or update.
	 * <p>
	 * Meant to be extended to provide functionality. This empty method simply allows ignoring, if desired.
	 *
	 * @param newOrChangedObject If true, object validated for creation. If false, validated for updating.
	 * @throws ValidationException If any validation issues arise
	 */
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, @Valid T newOrChangedObject, ClientSession clientSession) throws ValidationException {
		if(this.getClazz().isAssignableFrom(ImagedMainObject.class)){
			for(ObjectId curImageId : ((ImagedMainObject)newOrChangedObject).getImageIds()){
				try{
					this.imageService.getObj(oqmDbIdOrName, curImageId);
				} catch (DbNotFoundException e){
					throw new ValidationException("Image given not present in images: " + curImageId.toHexString(), e);
				}
			}
		}
		if(this.getClazz().isAssignableFrom(FileAttachmentContaining.class)){
			for(ObjectId curImageId : ((FileAttachmentContaining)newOrChangedObject).getAttachedFiles()){
				try{
					this.fileAttachmentService.getObj(oqmDbIdOrName, curImageId);
				} catch (DbNotFoundException e){
					throw new ValidationException("File Attachment given not present in file attachments: " + curImageId.toHexString(), e);
				}
			}
		}
	}
	
	protected <X extends CollectionStats.Builder<?,?>> X addBaseStats(String oqmDbIdOrName, X builder){
		return (X) builder.size(this.getTypedCollection(oqmDbIdOrName).countDocuments());
	}
	
	/**
	 * Todo:: extend this per service, subtypes, etc.
	 */
	public abstract V getStats(String oqmDbIdOrName);
	
	public abstract CollectionClearResult clear(String oqmDbIdOrName, @NonNull ClientSession session);
	
	public void runPostUpgrade(String oqmDbIdOrName, ClientSession cs, CollectionUpgradeResult upgradeResult) {
		//nothing to do.
	};
}
