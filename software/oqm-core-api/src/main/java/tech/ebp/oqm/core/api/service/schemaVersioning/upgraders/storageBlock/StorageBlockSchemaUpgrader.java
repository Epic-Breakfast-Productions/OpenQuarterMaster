package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.storageBlock;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;

import java.util.TreeSet;

@Slf4j
public class StorageBlockSchemaUpgrader extends ObjectSchemaUpgrader<StorageBlock> {

	public StorageBlockSchemaUpgrader() {
		super(
			StorageBlock.class,
			new TreeSet<>()
		);
	}
}
