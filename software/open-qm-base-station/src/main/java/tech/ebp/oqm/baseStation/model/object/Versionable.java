package tech.ebp.oqm.baseStation.model.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Versionable {
	
	/**
	 * The schema version of this object.
	 *
	 * This is used as reference in an associated {@link tech.ebp.oqm.baseStation.model.objectUpgrade.ObjectVersionBumper} and
	 * {@link tech.ebp.oqm.baseStation.model.objectUpgrade.ObjectUpgrader} to know what the current version is.
	 *
	 * Versions should start at `1`.
	 *
	 * @return The schema version of this object.
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public int getSchemaVersion();
}
