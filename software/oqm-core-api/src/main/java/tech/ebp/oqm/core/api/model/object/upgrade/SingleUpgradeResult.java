package tech.ebp.oqm.core.api.model.object.upgrade;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * The result from upgrading a single object.
 */
@Builder
@Data
public class SingleUpgradeResult {
	
	/**
	 * The object that was upgraded.
	 */
	ObjectNode upgradedObject;
	
	/**
	 * Objects created from upgrading the obejct.
	 */
	@NonNull
	@lombok.Builder.Default
	UpgradeCreatedObjectsResults createdObjects = new UpgradeCreatedObjectsResults();
	
	/**
	 * Determines if there were any created objects as a result of upgrading.
	 * @return if there were any created objects as a result of upgrading.
	 */
	public boolean hasCreatedObjects() {
		return !this.getCreatedObjects().isEmpty();
	}
}
