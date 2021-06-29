package com.ebp.openQuarterMaster.baseStation.data.mongo.storage;

import com.ebp.openQuarterMaster.baseStation.data.OurMongoEntity;
import io.quarkus.mongodb.panache.MongoEntity;
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
