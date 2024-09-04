package tech.ebp.oqm.core.api.model.object.storage.items;

import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;

/**
 * Enum to determine what type of storage the item uses.
 */
public enum StorageType {
	BULK(StoredType.AMOUNT),
	AMOUNT_LIST(StoredType.AMOUNT),
	UNIQUE_MULTI(StoredType.UNIQUE),
	UNIQUE_SINGLE(StoredType.UNIQUE),
	UNIQUE_GLOBAL(StoredType.UNIQUE)
	;

	public final StoredType storedType;

	StorageType(StoredType storedType) {
		this.storedType = storedType;
	}
}
