package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.Versionable;

/**
 * Abstract class to take an object from the next lower version to the version noted by {@link #bumperTo}
 *
 * @param <T>
 */
public abstract class ObjectVersionBumper<T extends Versionable> implements Comparable<ObjectVersionBumper<T>> {

	protected ObjectVersionBumper(int bumperTo) {
		this.bumperTo = bumperTo;
	}

	/**
	 * Notes what version this bumper bumps the object to.
	 */
	@Getter
	public final int bumperTo;

	/**
	 * Method to mutate the given object and return the resulting upgraded object.
	 * <p>
	 * The updates can happen either in-place, or using a copy of the object. It is recommended users of this method use the returned value rather than rely on pass-by-reference.
	 *
	 * @param oldObj The object to update
	 *
	 * @return The updated object
	 */
	public abstract JsonNode bumpObject(JsonNode oldObj);

	@Override
	public int compareTo(ObjectVersionBumper<T> tObjectVersionBumper) {
		return Integer.compare(
			this.getBumperTo(),
			tObjectVersionBumper.getBumperTo()
		);
	}
}
