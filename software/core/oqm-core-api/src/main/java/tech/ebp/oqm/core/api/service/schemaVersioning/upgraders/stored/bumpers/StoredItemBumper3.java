package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.identifiers.general.GeneralIdUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

public class StoredItemBumper3 extends ObjectSchemaVersionBumper<Stored> {
	
	public StoredItemBumper3() {
		super(3);
	}
	
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);
		
		if(oldObj.get("barcode") != null) {
			String oldBarcode = oldObj.get("barcode").asText();
			
			ArrayNode generalIds = oldObj.putArray("generalIds");
			
			if (oldBarcode != null && !oldBarcode.isBlank()) {
				Identifier identifier = GeneralIdUtils.determineGeneralIdType(oldBarcode);
				generalIds.add(ObjectUtils.OBJECT_MAPPER.valueToTree(identifier));
			}
		}
		oldObj.remove("barcode");
		
		UpgradingUtils.normalizeObjectId(oldObj, "item");
		UpgradingUtils.normalizeObjectId(oldObj, "storageBlock");
		if(oldObj.has("amount") && !oldObj.get("amount").isNull() && !oldObj.get("amount").isObject()){
			UpgradingUtils.deserializeJsonField(oldObj, "amount");
		}
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("imageIds"));
		UpgradingUtils.normalizeObjectIdList((ArrayNode) oldObj.get("attachedFiles"));
		
		return resultBuilder.build();
	}
}
