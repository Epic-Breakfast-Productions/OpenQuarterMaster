package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;

import java.util.TreeSet;

@Slf4j
public class StoredSchemaUpgrader extends ObjectSchemaUpgrader<Stored> {

	public StoredSchemaUpgrader() {
		super(
			Stored.class,
			new TreeSet<>()
		);
	}
}
