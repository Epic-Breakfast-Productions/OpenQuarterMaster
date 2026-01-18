package tech.ebp.oqm.core.api.testResources.data;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Random;

@ApplicationScoped
public class StoredTestObjectCreator extends TestObjectCreator<Stored> {
	private static final Random rand = new SecureRandom();

	@Setter
	private InventoryItem item;
	@Setter
	private ObjectId storageBlock;

	@Override
	public Stored getTestObject() {
		Stored.StoredBuilder<?, ?> builder = switch (item.getStorageType()) {
			case BULK, AMOUNT_LIST -> AmountStored.builder().amount(Quantities.getQuantity(0, this.item.getUnit()));
			case UNIQUE_MULTI, UNIQUE_SINGLE -> UniqueStored.builder();
		};
		builder
			.item(this.item.getId())
			.storageBlock(this.storageBlock);

		return builder.build();
	}
}
