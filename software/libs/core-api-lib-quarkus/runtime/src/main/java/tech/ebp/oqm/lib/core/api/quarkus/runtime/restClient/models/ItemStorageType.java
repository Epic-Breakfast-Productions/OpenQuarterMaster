package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models;

public enum ItemStorageType {
	BULK,
	/**
	 * The type to use where different amounts of stuff under the same block are useful. Examples; packed meat (different weights), milk (different expiration dates), shovels (different conditions)
	 */
	AMOUNT_LIST,
	/**
	 * The type to use where you store many unique items. Examples; anything with a serial number
	 */
	UNIQUE_MULTI,
	/**
	 * The type to use when your item is one-of-a-kind, and only exists in one place at a time. Examples; your favorite guitar
	 */
	UNIQUE_SINGLE
	;
}
