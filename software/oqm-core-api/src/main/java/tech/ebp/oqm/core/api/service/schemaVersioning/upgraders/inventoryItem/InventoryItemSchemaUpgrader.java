package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem;

import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;

import java.util.TreeSet;

/**
 * TODO:: figure out how to handle the subtypes
 */
public class InventoryItemSchemaUpgrader extends ObjectSchemaUpgrader<InventoryItem> {

	public InventoryItemSchemaUpgrader() {
		super(
			InventoryItem.class,
			new TreeSet<>()
		);
	}
}
