package tech.ebp.oqm.core.api.testResources.data;



import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import javax.money.Monetary;
import java.util.LinkedHashSet;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
	
	@Override
	public InventoryItem getTestObject() {
		InventoryItem item = new InventoryItem()
			.setName(faker.commerce().productName())
			.setDescription(faker.lorem().sentence())
			.setUnit(OqmProvidedUnits.UNIT)
								 .setDefaultPrices(new LinkedHashSet<>(){{
									 add(
										 StoredPricing.builder()
											 .label("testPrice")
											 .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											 .build()
									 );
								 }})
			.setStorageType(StorageType.BULK);
		
		return item;
	}
}
