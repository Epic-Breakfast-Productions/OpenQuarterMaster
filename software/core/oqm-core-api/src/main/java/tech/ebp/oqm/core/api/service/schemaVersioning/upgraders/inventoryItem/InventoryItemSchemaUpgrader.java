package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem;

import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers.InvItemBumper2;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers.InvItemBumper3;

import java.util.TreeSet;

/**
 *
 */
public class InventoryItemSchemaUpgrader extends ObjectSchemaUpgrader<InventoryItem> {

	public InventoryItemSchemaUpgrader() {
		super(
			InventoryItem.class,
			new InvItemBumper2(),
			new InvItemBumper3()
		);
	}
}
