package com.ebp.openQuarterMaster.baseStation.data.items;

import com.ebp.openQuarterMaster.baseStation.data.OurMongoEntity;
import io.quarkus.mongodb.panache.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "inventoryItem")
public class InventoryItem extends OurMongoEntity {
    private String name;
    private List<String> keywords = new ArrayList<>();
    private InventoryType inventoryType = InventoryType.COUNT;
    private Unit capMeasurement;

}
