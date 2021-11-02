package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.history.EventType;
import com.ebp.openQuarterMaster.lib.core.history.HistoryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import lombok.AllArgsConstructor;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.validation.ConstraintValidator;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 * <p>
 * TODO:: update
 * TODO:: add histories
 * TODO:: fully test
 *
 * @param <T> The type of object stored.
 */
@AllArgsConstructor
public abstract class MongoService<T extends MainObject> {

    protected final ObjectMapper objectMapper;
    protected final MongoClient mongoClient;
    protected final String database;
    protected final String collectionName;
    protected final Class<T> clazz;

    protected MongoCollection<T> collection = null;

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
                clazz.getSimpleName(),
                clazz,
                null
        );
    }

    protected MongoCollection<T> getCollection() {
        if (this.collection == null) {
            this.collection = mongoClient.getDatabase(this.database).getCollection(this.collectionName, this.clazz);
        }
        return this.collection;
    }


    /**
     * Gets a list of entries based on the options given.
     * <p>
     * TODO:: look into better, faster paging methods: https://dzone.com/articles/fast-paging-with-mongodb
     *
     * @param filter      The filter to use for the search. Nullable, no filter if null.
     * @param sort        The bson used to describe the sorting behavior. Nullable, no explicit sorting if null.
     * @param pageOptions The paging options. Nullable, not used if null.
     * @return a list of entries based on the options given.
     */
    public List<T> list(Bson filter, Bson sort, PagingOptions pageOptions) {
        List<T> list = new ArrayList<>();

        FindIterable<T> results;

        if (filter == null) {
            results = getCollection().find();
        } else {
            results = getCollection().find(filter);
        }

        if (sort != null) {
            results = results.sort(sort);
        }
        if (pageOptions != null) {
            results = results.skip(pageOptions.getSkipVal()).limit(pageOptions.pageSize);
        }

        results.into(list);

        return list;
    }

    /**
     * Gets a list of all elements in the collection.
     * <p>
     * Wrapper for {@link #list(Bson, Bson, PagingOptions)}, with all null arguments.
     *
     * @return a list of all elements in the collection.
     */
    public List<T> list() {
        return this.list(null, null, null);
    }

    public boolean collectionEmpty() {
        return this.getCollection().countDocuments() == 0;
    }

    /**
     * Gets the count of records in the collection using a filter.
     *
     * @param filter The filter to use. Nullable, gets the whole collection size if null.
     * @return the count of records in the collection
     */
    public long count(Bson filter) {
        if (filter == null) {
            return getCollection().countDocuments();
        }
        return this.getCollection().countDocuments(filter);
    }

    /**
     * Gets the count of all records in the collection.
     * <p>
     * Wrapper for {@link #count(Bson)}.
     *
     * @return the count of all records in the collection.
     */
    public long count() {
        return this.count(null);
    }

    /**
     * Gets an object with a particular id.
     *
     * @param objectId The id of the object to get
     * @return The object found. Null if not found.
     */
    public T get(ObjectId objectId) {
        T found = getCollection().find(eq("_id", objectId)).first();
        return found;
    }

    /**
     * Gets an object with a particular id.
     * <p>
     * Wrapper for {@link #get(ObjectId)}, to be able to use String representation of ObjectId.
     *
     * @param objectId The id of the object to get
     * @return The object found. Null if not found.
     */
    public T get(String objectId) {
        return this.get(new ObjectId(objectId));
    }

    public <A extends Annotation> T update(ObjectId id, ObjectNode updateJson, ConstraintValidator<A, T> validator) {
        if (updateJson.has("history")) {
            throw new IllegalArgumentException("Not allowed to update history of an object manually.");
        }
        T object = this.get(id);

        ObjectReader reader = objectMapper.readerForUpdating(object);
        try {
            reader.readValue(updateJson);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to update with data given: " + e.getMessage(), e);
        }

        if (!validator.isValid(object, null)) {
            throw new IllegalArgumentException("Unable to update with data given. Resulting object is invalid.");
        }

        object.updated(
                HistoryEvent.builder()
                        .userId(UUID.randomUUID())//TODO:: get id from jwt
                        .type(EventType.UPDATE)
                        .build()
        );

        this.getCollection().findOneAndReplace(eq("_id", id), object);
        return object;
    }

    public <A extends Annotation> T update(String id, ObjectNode updateJson, ConstraintValidator<A, T> validator) {
        return this.update(new ObjectId(id), updateJson, validator);
    }

    /**
     * Adds an object to the collection.
     *
     * @param object The object to add
     * @return The id of the newly added object.
     */
    public ObjectId add(T object) {
        if (!object.getHistory().isEmpty()) {
            throw new IllegalArgumentException("Object cannot have history before creation.");
        }

        object.updated(
                HistoryEvent.builder()
                        .userId(UUID.randomUUID())//TODO:: get id from jwt
                        .type(EventType.CREATE)
                        .build()
        );

        InsertOneResult result = getCollection().insertOne(object);

        return result.getInsertedId().asObjectId().getValue();
    }

    /**
     * Removes the object with the id given.
     *
     * @param objectId The id of the object to remove
     * @return The object that was removed
     */
    public T remove(ObjectId objectId) {
        T toRemove = this.get(objectId);

        if (toRemove == null) {
            return null;
        }

        toRemove.updated(
                HistoryEvent.builder()
                        .userId(UUID.randomUUID())//TODO:: get id from jwt
                        .type(EventType.REMOVE)
                        .build()
        );

        DeleteResult result = this.getCollection().deleteOne(eq("_id", objectId));

        return toRemove;
    }

    /**
     * Removes the object with the id given.
     * <p>
     * Wrapper for {@link #remove(ObjectId)}, to be able to use String representation of ObjectId.
     *
     * @param objectId The id of the object to remove
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
        return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
    }
}
