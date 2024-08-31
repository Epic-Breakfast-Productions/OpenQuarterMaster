package tech.ebp.oqm.core.api.testResources.data;



import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
	
	@Override
	public InventoryItem getTestObject() {
		InventoryItem item = new InventoryItem();

		item.setName(faker.commerce().productName());
		
		return item;
	}
}
