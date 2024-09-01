package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.validation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoDbAwareService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> extends MongoService<T, S, V> {
	
	
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
	 * The MongoDb client.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;
	
	/**
	 * The name of the database to access
	 */
	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String databasePrefix;

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
	
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<T> collection = null;
	
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
		log.debug("Getting collection for cache entry {}", db);
		if(!this.collections.containsKey(db.getDbId())){
			log.info("Collection for db cache entry not present. Creating. Cache entry: {}", db);
			this.collections.put(
				db.getDbId(),
				db.getMongoDatabase().getCollection(this.collectionName, this.clazz)
			);
		}
		return this.collections.get(db.getDbId());
	}

	public MongoCollection<T> getTypedCollection(String oqmDbIdOrName) {
		DbCacheEntry dbCacheEntry = this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName);

		return this.getTypedCollection(dbCacheEntry);
	}

	public MongoCollection<Document> getDocumentCollection(String oqmDbIdOrName) {
		DbCacheEntry dbCacheEntry = this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName);
		return dbCacheEntry.getMongoDatabase().getCollection(this.collectionName);
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
	
	public abstract long clear(String oqmDbIdOrName, @NonNull ClientSession session);
}
