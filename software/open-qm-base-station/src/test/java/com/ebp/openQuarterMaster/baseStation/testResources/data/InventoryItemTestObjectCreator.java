package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.lib.core.storage.items.AmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
    @Override
    public InventoryItem<?> getTestObject() {
        InventoryItem<?> item = new AmountItem();

        item.setName(faker.commerce().productName());

        return item;
    }
}
