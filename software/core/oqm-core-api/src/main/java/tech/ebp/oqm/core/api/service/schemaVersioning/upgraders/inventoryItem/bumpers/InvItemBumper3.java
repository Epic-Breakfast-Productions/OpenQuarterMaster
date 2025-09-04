package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.identifiers.general.GeneralIdUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class InvItemBumper3 extends ObjectSchemaVersionBumper<InventoryItem> {
	
	public InvItemBumper3() {
		super(3);
	}
	
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.Builder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);
		
		if(oldObj.get("barcode") != null) {
			String oldBarcode = oldObj.get("barcode").asText();
			
			ArrayNode generalIds = oldObj.putArray("generalIds");
			
			if (oldBarcode != null && !oldBarcode.isBlank()) {
				GeneralId generalId = GeneralIdUtils.determineGeneralIdType(oldBarcode);
				generalIds.add(ObjectUtils.OBJECT_MAPPER.valueToTree(generalId));
			}
		}
		oldObj.remove("barcode");
		
		//update existing fields
		UpgradingUtils.stringToConvertedTree(oldObj, "expiryWarningThreshold", Duration.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "unit", Unit.class);
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("storageBlocks"));
		
		if(oldObj.has("stats") && !oldObj.get("stats").isEmpty()){
			UpgradingUtils.stringToConvertedTree((ObjectNode) oldObj.get("stats"), "total", Quantity.class);
			
			for(JsonNode curBlockStats : oldObj.get("stats").get("storageBlockStats")){
				UpgradingUtils.stringToConvertedTree((ObjectNode) curBlockStats, "total", Quantity.class);
			}
		}
		
		return resultBuilder.build();
	}
}
