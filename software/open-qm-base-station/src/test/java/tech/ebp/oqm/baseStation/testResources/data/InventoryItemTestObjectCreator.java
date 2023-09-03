package tech.ebp.oqm.baseStation.testResources.data;



import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.SimpleAmountItem;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
	
	@Override
	public InventoryItem<?, ?, ?> getTestObject() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		item.setName(faker.commerce().productName());
		
		return item;
	}
}
