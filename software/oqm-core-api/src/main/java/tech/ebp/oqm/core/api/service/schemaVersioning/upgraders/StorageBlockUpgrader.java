package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.exception.VersionBumperListIncontiguousException;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

import java.util.SortedSet;
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
