package tech.ebp.oqm.core.api.testResources.data;



import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
	
	@Override
	public InventoryItem getTestObject() {
		InventoryItem item = new InventoryItem()
			.setName(faker.commerce().productName())
			.setDescription(faker.lorem().sentence())
			.setUnit(OqmProvidedUnits.UNIT)
			.setStorageType(StorageType.BULK);
		
		return item;
	}
}
