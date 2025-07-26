package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.checkout.bumpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

import java.util.Collection;
import java.util.List;

public class CheckoutBumper2 extends ObjectSchemaVersionBumper<ItemCheckout> {
	
	public CheckoutBumper2() {
		super(2);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		SingleUpgradeResult.Builder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj);
		
		//TODO
		
		
		return resultBuilder.build();
	}
}
