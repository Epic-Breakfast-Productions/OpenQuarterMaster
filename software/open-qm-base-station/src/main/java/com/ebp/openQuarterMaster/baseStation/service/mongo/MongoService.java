package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import lombok.AllArgsConstructor;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * TODO:: update
 * TODO:: add histories
 * TODO:: fully test
 * @param <T> The type of object stored.
 */
@AllArgsConstructor
public abstract class MongoService<T extends MainObject> {

    protected final MongoClient mongoClient;
    protected final String database;
    protected final String collectionName;
    protected final Class<T> clazz;

    protected MongoCollection<T> collection = null;

    protected MongoService(
            MongoClient mongoClient,
            String database,
            Class<T> clazz
    ) {
        this(
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
     * @param objectId The id of the object to get
     * @return The object found. Null if not found.
     */
    public T get(ObjectId objectId) {
        T found = getCollection().find(eq("_id", objectId)).first();
        return found;
    }

    /**
     * Gets an object with a particular id.
     *
     * Wrapper for {@link #get(ObjectId)}, to be able to use String representation of ObjectId.
     * @param objectId The id of the object to get
     * @return The object found. Null if not found.
     */
    public T get(String objectId) {
        return this.get(new ObjectId(objectId));
    }

    /**
     * Adds an object to the collection.
     * @param object The object to add
     * @return The id of the newly added object.
     */
    public ObjectId add(T object) {
        InsertOneResult result = getCollection().insertOne(object);

        return result.getInsertedId().asObjectId().getValue();
    }

    /**
     * Removes the object with the id given.
     * @param objectId The id of the object to remove
     * @return The object that was removed
     */
    public T remove(ObjectId objectId) {
        T toRemove = this.get(objectId);

        if (toRemove == null) {
            return null;
        }

        this.getCollection().deleteOne(eq("_id", objectId));
        return toRemove;
    }

    /**
     * Removes the object with the id given.
     *
     * Wrapper for {@link #remove(ObjectId)}, to be able to use String representation of ObjectId.
     * @param objectId The id of the object to remove
     * @return The object that was removed
     */
    public T remove(String objectId) {
        return this.remove(new ObjectId(objectId));
    }

    /**
     * Removes all items from the collection.
     * @return The number of items that were removed.
     */
    public long removeAll() {
        return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
    }
}
