package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.interactingEntity;

import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.interactingEntity.bumpers.IntEntBumper2;

/**
 *
 */
public class InteractingEntitySchemaUpgrader extends ObjectSchemaUpgrader<InteractingEntity> {

	public InteractingEntitySchemaUpgrader() {
		super(
			InteractingEntity.class,
			new IntEntBumper2()
		);
	}
}
