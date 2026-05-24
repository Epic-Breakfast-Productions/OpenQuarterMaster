package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.IndexOptions;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the main mongo class. It specifies top level, commonly shared utilities.
 * @param <T> The type of object this mongo service is dealing with
 * @param <S> The associated search object for the object
 * @param <V> The type of collection stats object to return
 */
@Slf4j
public abstract class MongoService<T extends MainObject, S extends SearchObject<T>, V extends CollectionStats> {

    // TODO:: move to constructor/inject? Remove?
    protected static final Validator VALIDATOR;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = validatorFactory.getValidator();
        }
    }

    /**
     * Gets the default transaction options to use for client sessions.
     *
     * @return The default transaction options.
     */
    public static TransactionOptions getDefaultTransactionOptions() {
        return TransactionOptions.builder().readPreference(ReadPreference.primary()).readConcern(ReadConcern.LOCAL).writeConcern(WriteConcern.MAJORITY).build();
    }

    public ClientSession getNewClientSession(boolean startTransaction) {
        ClientSession clientSession = this.getMongoClient().startSession();

        if (startTransaction) {
            clientSession.startTransaction();
        }

        return clientSession;
    }

    public ClientSession getNewClientSession() {
        return this.getNewClientSession(false);
    }

    /**
     * The default collection name to use when getting the collection.
     *
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

    public abstract List<Bson> getDbIndexes();

    public abstract void initDb();

    /**
     * Sets up indexes for the given MongoDB collection.
     * Creates any missing indexes and drops indexes that are no longer expected.
     * The default {@code _id_} index is never dropped.
     *
     * @param collection the MongoDB collection to manage indexes on
     * @param indexes    the list of indexes that should exist on the collection
     */
    protected static void setupIndexes(MongoCollection<?> collection, List<Bson> indexes) {
        IndexOptions options = new IndexOptions().background(true);
        Map<BsonDocument, String> existingIndexes = getExistingIndexes(collection);
        Set<BsonDocument> expectedKeys = new HashSet<>();

        for (Bson index : indexes) {
            BsonDocument expectedKey = index.toBsonDocument(BsonDocument.class, collection.getCodecRegistry());
            expectedKeys.add(expectedKey);
            if (!existingIndexes.containsKey(expectedKey)) {
                collection.createIndex(index, options);
            }
        }

        for (Map.Entry<BsonDocument, String> existing : existingIndexes.entrySet()) {
            String existingName = existing.getValue();
            if (!"_id_".equals(existingName) && !expectedKeys.contains(existing.getKey())) {
                collection.dropIndex(existingName);
            }
        }
    }

    /**
     * Returns a map of existing indexes on the given collection.
     * Input Index:
     * <pre>
     * {@code
     * Indexes.ascending("name")
     * }
     * </pre>
     *
     * Would produce:
     * <pre>
     * {@code
     *   Key: {"name": 1}
     *   Value: "name_1"
     * }
     * </pre>
     *
     * @param collection the MongoDB collection to read indexes from
     * @return map of index key document to index name
     */
    private static Map<BsonDocument, String> getExistingIndexes(MongoCollection<?> collection) {
        MongoIterable<Document> existingDocument = collection.listIndexes();
        Map<BsonDocument, String> existingIndexes = new HashMap<>();
        for (Document doc : existingDocument) {
            Document keyDoc = doc.get("key", Document.class);
            if (keyDoc == null) {
                continue;
            }
            BsonDocument key = keyDoc.toBsonDocument(BsonDocument.class, collection.getCodecRegistry());
            String name = doc.getString("name");
            if (name != null) {
                existingIndexes.put(key, name);
            }
        }
        return existingIndexes;
    }
}
