package com.ebp.openQuarterMaster.baseStation.data.mongo.items;

import com.ebp.openQuarterMaster.baseStation.data.mongo.OurMongoEntity;
import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "inventoryItem")
public class InventoryItemEntity extends OurMongoEntity<InventoryItem> {
}
