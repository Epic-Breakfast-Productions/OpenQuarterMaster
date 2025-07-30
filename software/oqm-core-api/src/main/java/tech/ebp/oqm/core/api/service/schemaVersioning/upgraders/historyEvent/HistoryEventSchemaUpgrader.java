package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.historyEvent;

import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.historyEvent.bumpers.HistoryEventBumper2;

/**
 *
 */
public class HistoryEventSchemaUpgrader extends ObjectSchemaUpgrader<ObjectHistoryEvent> {

	public HistoryEventSchemaUpgrader() {
		super(
			ObjectHistoryEvent.class,
			new HistoryEventBumper2()
		);
	}
}
