package com.ebp.openQuarterMaster.baseStation.data.mongo;

import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class InventoryItemService {
    @Inject
    MongoClient mongoClient;

    public List<InventoryItem> list(){
        List<InventoryItem> list = new ArrayList<>();
        MongoCursor<InventoryItem> cursor = getCollection().find().iterator();

        try {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public ObjectId add(InventoryItem item){
        InsertOneResult result = getCollection().insertOne(item);

        return result.getInsertedId().asObjectId().getValue();
    }

    private MongoCollection<InventoryItem> getCollection(){
        return mongoClient.getDatabase("openQuarterMaster").getCollection("inventoryItem", InventoryItem.class);
    }

}
