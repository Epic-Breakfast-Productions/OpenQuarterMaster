package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import lombok.AllArgsConstructor;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

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
    ){
        this(
                mongoClient,
                database,
                clazz.getSimpleName(),
                clazz,
                null
        );
    }

    protected MongoCollection<T> getCollection(){
        if(this.collection == null) {
            this.collection = mongoClient.getDatabase(this.database).getCollection(this.collectionName, this.clazz);
        }
        return this.collection;
    }

    public List<T> list(){
        List<T> list = new ArrayList<>();
        MongoCursor<T> cursor = getCollection().find().iterator();

        try {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public List<T> list(Bson filter){
        List<T> list = new ArrayList<>();
        MongoCursor<T> cursor = getCollection().find(filter).iterator();

        try {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public long count(){
        return getCollection().countDocuments();
    }

    //TODO:: count with filter

    public T get(ObjectId objectId){
        T found = getCollection().find(eq("_id", objectId)).first();
        return found;
    }
    public T get(String objectId){
        return this.get(new ObjectId(objectId));
    }

    //TODO:: get with filter
    //TODO:: paging with filter
    //  https://medium.com/swlh/mongodb-pagination-fast-consistent-ece2a97070f3

    public ObjectId add(T object){
        InsertOneResult result = getCollection().insertOne(object);

        return result.getInsertedId().asObjectId().getValue();
    }

    public T remove(ObjectId objectId){
        T toRemove = this.get(objectId);

        if(toRemove == null){
            return null;
        }

        this.getCollection().deleteOne(eq("_id", objectId));
        return toRemove;
    }

    public T remove(String objectId){
        return this.remove(new ObjectId(objectId));
    }

    public long removeAll(){
        return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
    }
}
