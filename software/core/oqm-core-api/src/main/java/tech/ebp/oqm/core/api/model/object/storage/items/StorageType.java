package tech.ebp.oqm.core.api.model.object.storage.items;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;

/**
 * Enum to determine what type of storage the item uses.
 */
public enum StorageType {

	/**
	 * The type to use for bulk amounts of stuff in a storage block. Examples; screws, mulch
	 */
//	@Schema(description = "A Bulk amount of something. Only one stored object per storage block allowed.")
	BULK(StoredType.AMOUNT),

	/**
	 * The type to use where different amounts of stuff under the same block are useful. Examples; packed meat (different weights), milk (different expiration dates), shovels (different conditions)
	 */
	@Deprecated(forRemoval = true, since = "6.0.0")
	AMOUNT_LIST(StoredType.AMOUNT),

	/**
	 * Type for multiple groupings of bulk amount items.
	 */
	BULK_GROUPS(StoredType.AMOUNT),

	/**
	 * Type for describing unique units with a quantity associated with it.
	 */
	UNIQUE_AMOUNT(StoredType.UNIQUE_WITH_AMOUNT),

	/**
	 * The type to use where you store many unique items. Examples; anything with a serial number
	 */
	UNIQUE_MULTI(StoredType.UNIQUE),

	/**
	 * The type to use when your item is one-of-a-kind, and only exists in one place at a time. Examples; your favorite guitar
	 */
	UNIQUE_SINGLE(StoredType.UNIQUE)
	;

	public final StoredType storedType;

	StorageType(StoredType storedType) {
		this.storedType = storedType;
	}
}
