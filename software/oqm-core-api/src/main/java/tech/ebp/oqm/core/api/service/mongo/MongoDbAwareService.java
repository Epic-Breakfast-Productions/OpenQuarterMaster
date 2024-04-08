package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.serviceState.db.MongoDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoDbAwareService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> {
	
	public static String getCollectionNameFromClass(Class<?> clazz) {
		return clazz.getSimpleName();
	}
	
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
	MongoDatabaseService mongoDatabaseService;
	
	/**
	 * The name of the collection this service is in charge of
	 */
	@Getter
	protected final String collectionName;
	
	/**
	 * The class this collection is in charge of. Used for logging.
	 */
	@Getter
	protected final Class<T> clazz;
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<T> collection = null;
	
	
	protected MongoDbAwareService(
		String collectionName,
		Class<T> clazz
	){
		this.collectionName = collectionName;
		this.clazz = clazz;
	}
	
	protected MongoDbAwareService(Class<T> clazz){
		this(getCollectionNameFromClass(clazz), clazz);
	}
	
	protected MongoDbAwareService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String databasePrefix,
		MongoDatabaseService mongoDatabaseService,
		String collectionName,
		Class<T> clazz
	) {
		this(collectionName, clazz);
		this.objectMapper = objectMapper;
		this.mongoClient = mongoClient;
		this.databasePrefix = databasePrefix;
		this.mongoDatabaseService = mongoDatabaseService;
	}
	
	/**
	 * TODO::
	 * @return
	 */
	protected MongoDatabase getMongoDatabase(){
		log.info("Database service: {}", this.getMongoDatabaseService());
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
			this.collection = this.getMongoDatabase().getCollection(this.collectionName, this.clazz);
		}
		return this.collection;
	}
	
	protected MongoCollection<T> getCollection(OqmMongoDatabase db) {
		//TODO
		return null;
	}
	
	protected MongoCollection<T> getCollection(String idOrName) {
		return this.getCollection(this.getMongoDatabaseService().getDatabase(idOrName));
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
	 */
	public void ensureObjectValid(boolean newObject, @Valid T newOrChangedObject, ClientSession clientSession) {
	}
	
	protected <X extends CollectionStats.Builder<?,?>> X addBaseStats(X builder){
		return (X) builder.size(this.getCollection().countDocuments());
	}
	
	/**
	 * Todo:: extend this per service, subtypes, etc.
	 */
	public abstract V getStats();
	
	public abstract long clear(@NonNull ClientSession session);
}
