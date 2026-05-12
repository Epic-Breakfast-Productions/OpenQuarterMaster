package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.identifiers.IdentifierUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.sharedOps.GenUnIdToIdentifierUpgrade;

import javax.measure.Unit;
import java.time.Duration;

public class StoredItemBumper4 extends ObjectSchemaVersionBumper<Stored> {
	
	public StoredItemBumper4() {
		super(4);
	}
	
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);
		
		GenUnIdToIdentifierUpgrade.upgradeIds(oldObj);
		GenUnIdToIdentifierUpgrade.upgradeStoredLabelFormat(oldObj, "labelFormat");
		
		//normal stuff
		UpgradingUtils.normalizeObjectId(oldObj, "item");
		UpgradingUtils.normalizeObjectId(oldObj, "storageBlock");
		UpgradingUtils.stringToConvertedTree(oldObj, "expiryWarningThreshold", Duration.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "unit", Unit.class);
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("imageIds"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("attachedFiles"));
		UpgradingUtils.dequoteString(oldObj, "expires");
		if(oldObj.has("amount") && !oldObj.get("amount").isNull() && !oldObj.get("amount").isObject()){
			UpgradingUtils.deserializeJsonField(oldObj, "amount");
		}
		UpgradingUtils.monetaryAmountMongoToJackson((ArrayNode) oldObj.get("prices"));
		
		
		return resultBuilder.build();
	}
}
