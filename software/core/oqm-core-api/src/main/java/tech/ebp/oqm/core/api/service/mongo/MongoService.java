package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

/**
 * This is the main mongo class. It specifies top level, commonly shared utilities.
 * @param <T> The type of object this mongo service is dealing with
 * @param <S> The associated search object for the object
 * @param <V> The type of collection stats object to return
 */
@Slf4j
public abstract class MongoService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> {
	
	//TODO:: move to constructor/inject? Remove?
	protected static final Validator VALIDATOR;

	static {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			VALIDATOR = validatorFactory.getValidator();
		}
	}

	/**
	 * Gets the default transaction options to use for client sessions.
	 * @return The default transaction options.
	 */
	public static TransactionOptions getDefaultTransactionOptions() {
		return TransactionOptions.builder()
			.readPreference(ReadPreference.primary())
			.readConcern(ReadConcern.LOCAL)
			.writeConcern(WriteConcern.MAJORITY)
			.build();
	}

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
	 * The default collection name to use when getting the collection.
	 * @param clazz The class to get the collection of
	 * @return The collection name to use when getting the collection.
	 */
	public static String getCollectionNameFromClass(Class<?> clazz) {
		return clazz.getSimpleName();
	}
	
	/**
	 * The class this collection is in charge of. Used for logging, and other fun.
	 */
	@Getter
	protected final Class<T> clazz;
	
	/**
	 * The MongoDb client.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;

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

	public MongoService(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public abstract int getCurrentSchemaVersion();
}
