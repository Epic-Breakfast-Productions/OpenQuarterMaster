package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.identifiers.IdentifierUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.sharedOps.GenUnIdToIdentifierUpgrade;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;

public class InvItemBumper4 extends ObjectSchemaVersionBumper<InventoryItem> {
	
	public InvItemBumper4() {
		super(4);
	}
	
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);
		
		GenUnIdToIdentifierUpgrade.upgradeIds(oldObj);
		GenUnIdToIdentifierUpgrade.upgradeStoredLabelFormat(oldObj, "defaultLabelFormat");
		
		//update existing fields
		UpgradingUtils.stringToConvertedTree(oldObj, "expiryWarningThreshold", Duration.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "unit", Unit.class);
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("storageBlocks"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("imageIds"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("attachedFiles"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("categories"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("idGenerators"));
		
		if(oldObj.has("stats") && !oldObj.get("stats").isEmpty()){
			UpgradingUtils.stringToConvertedTree((ObjectNode) oldObj.get("stats"), "total", Quantity.class);
			
			for(JsonNode curBlockStats : oldObj.get("stats").get("storageBlockStats")){
				UpgradingUtils.stringToConvertedTree((ObjectNode) curBlockStats, "total", Quantity.class);
			}
		}
		
		
		return resultBuilder.build();
	}
}
