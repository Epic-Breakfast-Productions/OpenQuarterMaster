package com.ebp.openQuarterMaster.baseStation.data.mongo.items;

import com.ebp.openQuarterMaster.baseStation.data.OurMongoEntity;
import com.ebp.openQuarterMaster.baseStation.data.pojos.TrackType;
import io.quarkus.mongodb.panache.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "inventoryItem")
public class InventoryItem extends OurMongoEntity {
    private String name;
    private List<String> keywords = new ArrayList<>();
    private TrackType trackType = TrackType.COUNT;
    private String capacityMeasurement;
}
