package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.interactingEntity.bumpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;

public class IntEntBumper2 extends ObjectSchemaVersionBumper<InteractingEntity> {
	
	public IntEntBumper2() {
		super(2);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj);

		oldObj.put("type", oldObj.get("interactingEntityType"));
		oldObj.remove("interactingEntityType");
		
		return resultBuilder.build();
	}
}
