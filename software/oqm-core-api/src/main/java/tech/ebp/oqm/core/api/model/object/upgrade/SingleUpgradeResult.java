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
	private ObjectNode upgradedObject;
	
	/**
	 * Objects created from upgrading the object.
	 */
	@NonNull
	@lombok.Builder.Default
	private UpgradeCreatedObjectsResults createdObjects = new UpgradeCreatedObjectsResults();
	
	@lombok.Builder.Default
	private boolean delObj = false;
	
	/**
	 * Determines if there were any created objects as a result of upgrading.
	 * @return if there were any created objects as a result of upgrading.
	 */
	public boolean hasCreatedObjects() {
		return !this.getCreatedObjects().isEmpty();
	}
}
