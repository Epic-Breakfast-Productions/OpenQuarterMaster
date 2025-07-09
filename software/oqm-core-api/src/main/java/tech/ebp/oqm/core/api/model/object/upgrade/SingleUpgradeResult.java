package tech.ebp.oqm.core.api.model.object.upgrade;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class SingleUpgradeResult {
	ObjectNode upgradedObject;
	
	@NonNull
	@lombok.Builder.Default
	UpgradeCreatedObjectsResults createdObjects = new UpgradeCreatedObjectsResults();
	
	public boolean hasCreatedObjects() {
		return !this.getCreatedObjects().isEmpty();
	}
}
