package tech.ebp.oqm.core.api.model.object.upgrade;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface to determine if a result resulted in any upgraded.
 */
public interface WasUpgraded {
	
	/**
	 * Determines if any objects were upgraded.
	 * @return If any objects were upgraded.
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	boolean wasUpgraded();
}
