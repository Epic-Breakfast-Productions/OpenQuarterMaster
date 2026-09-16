package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class StoredItemBumper2 extends ObjectSchemaVersionBumper<Stored> {
	
	public StoredItemBumper2() {
		super(2);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj);
		UpgradingUtils.normalizeObjectId(oldObj, "item");
		UpgradingUtils.normalizeObjectId(oldObj, "storageBlock");
		
		if(oldObj.has("amount") && !oldObj.get("amount").isNull() && !oldObj.get("amount").isObject()){
			UpgradingUtils.deserializeJsonField(oldObj, "amount");
		}
		
		if(oldObj.has("expires") && !oldObj.get("expires").isNull()){
			UpgradingUtils.normalizeObject(oldObj, "expires", "expires", "$date");
			
			//Mongo internalizes a LocalDateTime as a UTC zoned string
			String expiredOldLocalDtStr = oldObj.get("expires").asText();
			ZonedDateTime expiredOldLocalDt = ZonedDateTime.parse(expiredOldLocalDtStr);
			
			oldObj.put("expires", expiredOldLocalDt.toString());
		}
		
		return resultBuilder.build();
	}
}
