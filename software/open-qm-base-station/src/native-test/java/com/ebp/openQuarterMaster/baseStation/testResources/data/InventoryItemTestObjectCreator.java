package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
    @Override
    public InventoryItem getTestObject() {
        InventoryItem item = new InventoryItem();

        item.setName(faker.commerce().productName());
        item.setStoredType(StoredType.AMOUNT);

        return item;
    }
}
