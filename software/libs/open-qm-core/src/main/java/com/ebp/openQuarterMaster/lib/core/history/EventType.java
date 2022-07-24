package com.ebp.openQuarterMaster.lib.core.history;

/**
 * Used with {@link ObjectHistory} to describe the type of event.
 */
public enum EventType {
	/** A creation event; marking the creation of the thing in the database. */
	CREATE,
	/** An event describing the addition of something */
	ADD,
	/** An event describing the removal of something */
	REMOVE,
	/** An event describing the update of details */
	UPDATE,
	/** An event describing the deletion of something */
	DELETE
}
