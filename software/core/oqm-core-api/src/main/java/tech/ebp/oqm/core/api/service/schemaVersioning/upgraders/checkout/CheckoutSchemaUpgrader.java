package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.checkout;

import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.checkout.bumpers.CheckoutBumper2;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.historyEvent.bumpers.HistoryEventBumper2;

/**
 *
 */
public class CheckoutSchemaUpgrader extends ObjectSchemaUpgrader<ItemCheckout> {
	
	public CheckoutSchemaUpgrader() {
		super(
			ItemCheckout.class,
			new CheckoutBumper2()
		);
	}
}
