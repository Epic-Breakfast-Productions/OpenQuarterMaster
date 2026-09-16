package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;

import javax.measure.Quantity;

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

/**
 * Abstract class to take an object from the next lower version to the version noted by {@link #bumperTo}
 *
 * @param <T>
 */
public abstract class ObjectSchemaVersionBumper<T extends Versionable> implements Comparable<ObjectSchemaVersionBumper<T>> {
	
	public static final String SCHEMA_VERSION_FIELD = "schemaVersion";
	
	protected ObjectSchemaVersionBumper(int bumperTo) {
		this.bumperTo = bumperTo;
	}
	
	/**
	 * Notes what version this bumper bumps the object to.
	 */
	@Getter
	public final int bumperTo;
	
	/**
	 * The schema version this bumper is intended to bump from.
	 *
	 * @return
	 */
	public int getBumperFrom() {
		return this.getBumperTo() - 1;
	}
	
	
	/**
	 * Method to mutate the given object and return the resulting upgraded object.
	 * <p>
	 * The updates can happen either in-place, or using a copy of the object. It is recommended users of this method use the returned value rather than rely on pass-by-reference.
	 *
	 * @param oldObj The object to update
	 *
	 * @return The updated object
	 */
	public SingleUpgradeResult bumpObject(ObjectNode oldObj) {
//		log.debug("Bumping object from version {} to version {}", oldObj.get(SCHEMA_VERSION_FIELD).asInt(), this.getBumperTo());
		if(!oldObj.has(SCHEMA_VERSION_FIELD)) {
			throw new IllegalArgumentException("Object given must have schema version field.");
		}
		if(this.getBumperFrom() != oldObj.get(SCHEMA_VERSION_FIELD).asInt()) {
			throw new IllegalArgumentException(
				"Cannot upgrade object of version " + oldObj.get(SCHEMA_VERSION_FIELD).asInt() + " to " + this.getBumperTo() + ". Expected version " + " " + this.getBumperFrom()
			);
		}
		
		SingleUpgradeResult result = this.bumpObjectSchema(oldObj);
		this.bumpSchemaVersion(result.getUpgradedObject());
		return result;
	}
	
	/**
	 * Performs the actual schema mutation.
	 *
	 * @param oldObj The object to update.
	 *
	 * @return The updated object
	 */
	protected abstract SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj);
	
	/**
	 * Use this to set the new schema version for upgraded objects.
	 *
	 * @param oldObj The json representation of the object we are bumping
	 *
	 * @return The same object node given, with .
	 */
	protected ObjectNode bumpSchemaVersion(ObjectNode oldObj) {
		oldObj.put(SCHEMA_VERSION_FIELD, this.getBumperTo());
		return oldObj;
	}
	
	@Override
	public int compareTo(ObjectSchemaVersionBumper<T> tObjectSchemaVersionBumper) {
		return Integer.compare(
			this.getBumperTo(),
			tObjectSchemaVersionBumper.getBumperTo()
		);
	}
}
