package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers.StoredItemBumper2;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers.StoredItemBumper3;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers.StoredItemBumper4;

import java.util.TreeSet;

@Slf4j
public class StoredSchemaUpgrader extends ObjectSchemaUpgrader<Stored> {

	public StoredSchemaUpgrader() {
		super(
			Stored.class,
			new StoredItemBumper2(),
			new StoredItemBumper3(),
			new StoredItemBumper4()
		);
	}
}
