package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.storageBlock;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectUpgrader;

import java.util.TreeSet;

@Slf4j
public class StorageBlockUpgrader extends ObjectUpgrader<StorageBlock> {

	public StorageBlockUpgrader() {
		super(
			StorageBlock.class,
			new TreeSet<>()
		);
	}
}
