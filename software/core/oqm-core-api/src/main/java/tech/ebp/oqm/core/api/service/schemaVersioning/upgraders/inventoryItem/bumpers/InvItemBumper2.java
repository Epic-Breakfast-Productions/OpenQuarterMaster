package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
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

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

public class InvItemBumper2 extends ObjectSchemaVersionBumper<InventoryItem> {
	
	public InvItemBumper2() {
		super(2);
	}
	
	public ObjectNode adjustStored(String itemId, ObjectNode oldStored, String storageBlock) {
		oldStored.remove("id");
		oldStored.remove("_id");
		oldStored.remove("_t");
		oldStored.put("id", new ObjectId().toHexString());
		oldStored.put("item", itemId);
		oldStored.put("storageBlock", storageBlock);
		oldStored.put(SCHEMA_VERSION_FIELD, 1);
		oldStored.set("type", oldStored.get("storedType"));
		oldStored.remove("storedType");
		
		if(oldStored.has("amount")){
			UpgradingUtils.stringToConvertedTree(
				oldStored,
				"amount",
				Quantity.class
			);
		}
		if(oldStored.has("identifier")){
			((ObjectNode)oldStored.get("attributes"))
				.put("oldIdentifier", oldStored.get("identifier").asText());
			oldStored.remove("identifier");
		}
		oldStored.remove("identifyingDetails");
		if("TRACKED".equals(oldStored.get("type").asText())){
			oldStored.put("type", "UNIQUE");
		}
		
		return oldStored;
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		String itemId = oldObj.get("id").asText();
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj)
														.createdObjects(createdObjectsResults);
		
		List<ObjectNode> newStoredList = new ArrayList<>();
		String oldType = oldObj.get("storageType").asText();
		ObjectNode oldStorageMap = (ObjectNode) oldObj.get("storageMap");
		ArrayNode storageBlocks = oldObj.putArray("storageBlocks");
		
		createdObjectsResults.put(Stored.class, newStoredList);
		
		//remove old fields
		oldObj.remove("storageMap");
		oldObj.remove("total");
		oldObj.remove("valueOfStored");
		oldObj.remove("numLowStock");
		oldObj.remove("numExpired");
		oldObj.remove("numExpiryWarn");
		oldObj.remove("valuePerUnit");
		oldObj.remove("defaultValue");
		oldObj.remove("trackedItemIdentifierName");
		
		//update existing fields
		UpgradingUtils.stringToConvertedTree(oldObj, "expiryWarningThreshold", Duration.class);
		UpgradingUtils.stringToConvertedTree(oldObj, "unit", Unit.class);
		
		//add new fields
		oldObj.putNull("stats");
		
		//populate storage blocks
		storageBlocks.addAll(
			StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(oldStorageMap.fieldNames(), Spliterator.ORDERED),
					false
				)
				.map(storageBlocks::textNode)
				.toList()
		);
		
		//update type-specific entries
		switch (oldType) {
			case "AMOUNT_SIMPLE":
				oldObj.put("storageType", "BULK");
				break;
			case "AMOUNT_LIST":
				oldObj.put("storageType", "AMOUNT_LIST");
				break;
			case "TRACKED":
				oldObj.put("storageType", "UNIQUE_MULTI");
				break;
		}
		
		//any stored entries in storage map turned to created stored objects
		for (Iterator<String> it = oldStorageMap.fieldNames(); it.hasNext(); ) {
			String curBlock = it.next();
			ObjectNode underBlock = (ObjectNode) oldStorageMap.get(curBlock);
			
			switch (oldType) {
				case "AMOUNT_SIMPLE": {
					ObjectNode newStored = (ObjectNode) underBlock.get("stored");
					if (newStored != null) {
						newStoredList.add(adjustStored(itemId, newStored, curBlock));
					}
				}
				break;
				case "AMOUNT_LIST": {
					ArrayNode storedList = (ArrayNode) underBlock.get("stored");
					if (storedList != null) {
						storedList.forEach((JsonNode newStored)->{
							newStoredList.add(adjustStored(itemId, (ObjectNode) newStored, curBlock));
						});
					}
				}
				break;
				case "TRACKED": {
					ObjectNode storedMap = (ObjectNode) underBlock.get("stored");
					if (storedMap != null) {
						storedMap.forEach((JsonNode newStored)->{
							newStoredList.add(adjustStored(itemId, (ObjectNode) newStored, curBlock));
						});
					}
				}
				break;
			}
		}
		
		return resultBuilder.build();
	}
}
