package com.ebp.openQuarterMaster.baseStation.data.mongo.storage;

import com.ebp.openQuarterMaster.baseStation.data.mongo.OurMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "storage")
public class StorageBlock extends OurMongoEntity {
    private String name;
    private String identifier;
    private ZonedDateTime added;
    private boolean active;
}
