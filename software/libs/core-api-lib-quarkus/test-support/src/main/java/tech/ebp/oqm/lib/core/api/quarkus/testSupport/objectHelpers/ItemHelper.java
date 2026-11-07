package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemHelper extends CoreApiLibClientHelper {
	private static final AtomicInteger count = new AtomicInteger();

	/**
	 * <code>
	 *     {
	 *   "attributes": {},
	 *   "keywords": [],
	 *   "imageIds": [],
	 *   "label": "Shed",
	 *   "nickname": "Junk Drawer",
	 *   "description": "",
	 *   "location": "",
	 *   "parent": "null",
	 *   "capacityMeasures": null,
	 *   "storedCategories": [],
	 *   "attachedFiles": []
	 * }
	 * </code>
	 * @return
	 */
	public static ObjectNode getItemTemplate(
		String storageType,
		ArrayNode storageBlocks
	){
		ObjectNode output = getObjectMapper().createObjectNode();

		output.put("name", count.incrementAndGet() + "-" +getFaker().food().ingredient());
		output.put("storageType", storageType);
		output.put("description", getFaker().lorem().paragraph());

		if(storageBlocks != null){
			output.set("storageBlocks", storageBlocks);
		}

		return output;
	}

	public static ObjectNode getItemTemplate(
		String storageType
	){
		return getItemTemplate(storageType, null);
	}

	public static ObjectNode storageIdsToStorageSetting(String blockId) {
		return getObjectMapper().createObjectNode()
				   .put("storageBlock", blockId);
	}

	public static ArrayNode storageIdsToStorageSettings(String... blockIds){
		ArrayNode output = getObjectMapper().createArrayNode();

		for(String curId : blockIds){
			output.add(storageIdsToStorageSetting(curId));
		}

		return output;
	}

	public static ArrayNode storageIdsToStorageSettings(ObjectNode ... blocks){
		return storageIdsToStorageSettings(Arrays.stream(blocks).map((b)->b.get("id").asText()).toArray(String[]::new));
	}

	public static ArrayNode storageIdsToStorageSettings(Collection<ObjectNode> blocks){
		return storageIdsToStorageSettings(blocks.toArray(ObjectNode[]::new));
	}


	public static ObjectNode addItem(
		String auth,
		String oqbDbNameOrId,
		ObjectNode inventoryItem
	){
		return given()
				   .header(new Header("Authorization", auth))
				   .accept(ContentType.JSON)
				   .contentType(ContentType.JSON)
				   .body(inventoryItem)
				   .when()
				   .pathParam("db", oqbDbNameOrId)
				   .post(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/db/{db}/inventory/item")
				   .then()
				   .statusCode(200)
				   .extract().body().as(ObjectNode.class);
	}
}
