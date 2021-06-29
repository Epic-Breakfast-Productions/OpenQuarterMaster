package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class InventoryItem {
    private String name;
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    @Builder.Default
    private InventoryType inventoryType = InventoryType.COUNT;
    private String capacityMeasurement;
}
