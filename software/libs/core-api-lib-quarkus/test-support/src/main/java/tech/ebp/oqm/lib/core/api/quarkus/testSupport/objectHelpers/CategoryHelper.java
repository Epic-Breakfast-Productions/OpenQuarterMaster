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
public final class CategoryHelper extends CoreApiLibClientHelper {
	private static final AtomicInteger count = new AtomicInteger();

	/**
	 * <code>
	 *     {
	 *   "name": "",
	 *   "description": "",
	 *   "color": "",
	 *   "parent": "",
	 * }
	 * </code>
	 * @return
	 */
	public static ObjectNode getCategoryTemplate(
		String parent
	){
		ObjectNode output = getObjectMapper().createObjectNode();

		output.put("name", count.incrementAndGet() + "-" + getFaker().brand().watch());
		output.put("description", getFaker().lorem().paragraph());

		if(getRand().nextBoolean()){
    		output.put("color", getFaker().color().hex());
		}

		if(parent != null){
			output.put("parent", parent);
		}

		return output;
	}

	public static ObjectNode getCategoryTemplate(
	){
		return getCategoryTemplate(null);
	}

	public static ObjectNode addCategory(
		String auth,
		String oqbDbNameOrId,
		ObjectNode category
	){
		return given()
				   .header(new Header("Authorization", auth))
				   .accept(ContentType.JSON)
				   .contentType(ContentType.JSON)
				   .body(category)
				   .when()
				   .pathParam("db", oqbDbNameOrId)
				   .post(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/db/{db}/inventory/item-category")
				   .then()
				   .statusCode(200)
				   .extract().body().as(ObjectNode.class);
	}
}
