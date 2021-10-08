package com.ebp.openQuarterMaster.baseStation.data.mongo.storage;

import com.ebp.openQuarterMaster.baseStation.data.mongo.OurMongoEntity;
import com.ebp.openQuarterMaster.lib.core.storage.StorageSpace;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "storageBlock")
public class StorageBlockEntity extends OurMongoEntity<StorageSpace> {
}
