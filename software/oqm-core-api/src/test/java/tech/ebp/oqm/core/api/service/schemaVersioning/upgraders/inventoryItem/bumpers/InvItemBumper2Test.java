package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.testResources.testClasses.SchemaBumperTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * TODO:: figure out what part of this is broken, there's a mismatch in the expected data
 */
@Disabled
@Slf4j
public class InvItemBumper2Test extends SchemaBumperTest<InvItemBumper2> {
	
	public InvItemBumper2Test(TestInfo testInfo) {
		super(new InvItemBumper2());
	}
	
	@SneakyThrows
	private static ObjectNode getSameItem() {
		ObjectNode node = (ObjectNode) OBJECT_MAPPER.readTree("""
			{
				"id": "68425e7cf47b86765eaa7a18",
				"attributes": {"foo":"bar"},
				"keywords": ["fooBar"],
				"imageIds": ["68425e7cf47b86765eaa7a18"],
				"name": "Book",
				"description": "Lorem Ipsum",
				"barcode": "123456",
				"categories": ["68425e7cf47b86765eaa7a18"],
				"attachedFiles": ["68425e7cf47b86765eaa7a18"],
				"notificationStatus": {
					"lowStock": false
				},
				"expiryWarningThreshold": 0,
				"lowStockThreshold": null,
				"unit": {
					"string": "units",
					"name": "Units",
					"symbol": "units"
				}
			}
			""");
		return node;
	}
	
	@SneakyThrows
	private static ObjectNode getCommonStored() {
		ObjectNode node = (ObjectNode) OBJECT_MAPPER.readTree("""
			{
				"barcode": "",
				"expires": null,
				"notificationStatus": {
					"expired": false,
					"expiredWarning": false
				},
				"condition": null,
				"conditionNotes": "",
				"imageIds": [],
				"attributes": {},
				"keywords": [],
				"storedType": "AMOUNT",
				"labelText": "0 units"
			 			}
			""");
		return node;
	}
	
	
	@SneakyThrows
	public static Stream<Arguments> getObjects() {
		List<Arguments> output = new ArrayList<>();
		
		{//bulk
			ObjectNode oldObj = getSameItem();
			ObjectNode newObj = oldObj.deepCopy();
			Map<Class<?>, List<ObjectNode>> createdNodes = new HashMap<>();
			
			//setup old
			oldObj.put("storageType", "AMOUNT_SIMPLE");
			oldObj.put("schemaVersion", 1);
			oldObj.put("valueOfStored", 1);
			oldObj.put("numLowStock", 1);
			oldObj.put("numExpired", 1);
			oldObj.put("numExpiryWarn", 1);
			oldObj.put("valuePerUnit", 1);
			
			ObjectNode total = oldObj.putObject("total").put("foo", "bar");
			ObjectNode storageMap = oldObj.putObject("storageMap");
			List<ObjectNode> newAmounts = new ArrayList<>();
			{
				ObjectNode storedWrapper = storageMap.putObject("68425e7cf47b86765eaa7a18");
				storedWrapper.set("stored", getCommonStored());
				ObjectNode stored = (ObjectNode) storedWrapper.get("stored");
				stored.put("id", UUID.randomUUID().toString());
				stored.put("storedType", "AMOUNT");
				stored.set(
					"amount", OBJECT_MAPPER.readTree("""
						{
											"unit": {
												"string": "units",
												"name": "Units",
												"symbol": "units"
											},
											"scale": "ABSOLUTE",
											"value": 0
										}
						""")
				);
				
				ObjectNode newStored = stored.deepCopy();
				newStored.remove("id");
				newStored.put("storageBlock", "68425e7cf47b86765eaa7a18");
				newAmounts.add(newStored);
			}
			{
				ObjectNode storedWrapper = storageMap.putObject("68425e7cf47b86765eaa7a19");
				storedWrapper.set("stored", getCommonStored());
				ObjectNode stored = (ObjectNode) storedWrapper.get("stored");
				stored.put("id", UUID.randomUUID().toString());
				stored.put("storedType", "AMOUNT");
				stored.set(
					"amount", OBJECT_MAPPER.readTree("""
						{
											"unit": {
												"string": "units",
												"name": "Units",
												"symbol": "units"
											},
											"scale": "ABSOLUTE",
											"value": 0
										}
						""")
				);
				
				
				ObjectNode newStored = stored.deepCopy();
				newStored.remove("id");
				newStored.put("storageBlock", "68425e7cf47b86765eaa7a19");
				newStored.set("item", oldObj.get("id"));
				newStored.put("schemaVersion", 1);
				stored.put("type", "AMOUNT");
				stored.remove("storageType");
				
				newAmounts.add(newStored);
			}
			createdNodes.put(Stored.class, newAmounts);
			
			//setup new
			newObj.put("storageType", "BULK");
			newObj.put("schemaVersion", 2);
			newObj.putNull("stats");
			newObj.putArray("storageBlocks").addAll(
				Stream.of("68425e7cf47b86765eaa7a18", "68425e7cf47b86765eaa7a19").map(newObj::textNode).toList()
			);
			
			output.add(Arguments.of(oldObj, newObj, createdNodes));
		}
		//TODO:: amount list
		//TODO:: tracked
		
		
		return output.stream();
	}
}
