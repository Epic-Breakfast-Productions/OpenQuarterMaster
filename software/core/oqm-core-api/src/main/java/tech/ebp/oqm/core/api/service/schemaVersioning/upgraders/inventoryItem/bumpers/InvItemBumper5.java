package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.sharedOps.GenUnIdToIdentifierUpgrade;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

@Slf4j
public class InvItemBumper5 extends ObjectSchemaVersionBumper<InventoryItem> {

	public InvItemBumper5() {
		super(5);
	}


	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);

		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("storageBlocks"));
		ArrayNode oldBlockList = (ArrayNode) oldObj.get("storageBlocks");
		ArrayNode newBlockList = oldObj.putArray("storageBlocks");

		for(JsonNode curBlock : oldBlockList){
			log.info("DEBUG:: Adding new settings for storage block: {}", curBlock);
			ObjectNode newSettings = newBlockList.addObject();
			newSettings.set("storageBlock", curBlock);
		}

		//update existing fields
		UpgradingUtils.stringToConvertedTree(oldObj, "expiryWarningThreshold", Duration.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "unit", Unit.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "lowStockThreshold", Quantity.class);
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

		UpgradingUtils.monetaryAmountMongoToJackson((ArrayNode) oldObj.get("defaultPrices"));
		UpgradingUtils.monetaryAmountMongoToJackson((ArrayNode) oldObj.get("stats").get("prices"));

		for(JsonNode curBlockStat : oldObj.get("stats").get("storageBlockStats")){
			UpgradingUtils.monetaryAmountMongoToJackson((ArrayNode) curBlockStat.get("prices"));
		}

		return resultBuilder.build();
	}
}
