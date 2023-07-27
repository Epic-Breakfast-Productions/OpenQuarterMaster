package tech.ebp.oqm.baseStation.service.mongo;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@AllArgsConstructor
@Slf4j
public abstract class MongoService<T extends MainObject, S extends SearchObject<T>> {
	
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
	@Getter(AccessLevel.PROTECTED)
	private final ObjectMapper objectMapper;
	/**
	 * The MongoDb client.
	 */
	@Getter(AccessLevel.PROTECTED)
	private final MongoClient mongoClient;
	/**
	 * The name of the database to access
	 */
	protected final String database;
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
	
	protected MongoService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz
	) {
		this(
			objectMapper,
			mongoClient,
			database,
			getCollectionNameFromClass(clazz),
			clazz,
			null
		);
	}
	
	protected MongoDatabase getDatabase(){
		return this.getMongoClient().getDatabase(this.database);
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
			this.collection = this.getDatabase().getCollection(this.collectionName, this.clazz);
		}
		return this.collection;
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
	
}
