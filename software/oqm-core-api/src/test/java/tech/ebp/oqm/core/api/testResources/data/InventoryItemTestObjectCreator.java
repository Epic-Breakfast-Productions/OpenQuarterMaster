package tech.ebp.oqm.core.api.testResources.data;



import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.SimpleAmountItem;

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
