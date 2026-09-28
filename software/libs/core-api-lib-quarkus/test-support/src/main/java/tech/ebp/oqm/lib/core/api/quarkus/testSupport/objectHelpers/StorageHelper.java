package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageHelper extends CoreApiLibClientHelper {
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
	public static ObjectNode getStorageBlockTemplate(
	){
		ObjectNode output = getObjectMapper().createObjectNode();

		output.put("label", count.incrementAndGet() + "-" + getFaker().location().building());

		return output;
	}

	public static ObjectNode addStorageBlock(
		String auth,
		String oqbDbNameOrId,
		ObjectNode storageBlock
	){
		return given()
				   .header(new Header("Authorization", auth))
				   .accept(ContentType.JSON)
				   .contentType(ContentType.JSON)
				   .body(storageBlock)
				   .when()
				   .pathParam("db", oqbDbNameOrId)
				   .post(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/db/{db}/inventory/storage-block")
				   .then()
				   .statusCode(200)
				   .extract().body().as(ObjectNode.class);
	}
}
