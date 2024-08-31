package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import jakarta.inject.Inject;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	
	@Inject
	ObjectMapper objectMapper;
	
	@Inject
	InventoryItemService inventoryItemService;


	
	@Inject
	@Location("templates/items.csv")
	Template itemsCsv;


	//TODO:: CRUD of inventory items

	//TODO
//	@Test
//	public void testAddFromCsv() throws IOException {
//		User user = this.getTestUserService().getTestUser();
//
//		String csvData = this.itemsCsv.render();
//
//		csvData += System.lineSeparator() + "Test Simple, testing a simple amount item, AMOUNT_SIMPLE,,1.00,,,";
//		csvData += System.lineSeparator() + "Test List, testing a list amount item, AMOUNT_LIST,,1.00,,,";
//		csvData += System.lineSeparator() + "Test Tracked, testing a tracked item, TRACKED,,1.00,,serial,";
//
//
//		ImportBundleFileBody body = new ImportBundleFileBody();
//		body.file = new ByteArrayInputStream(csvData.getBytes());
//		body.fileName = "test.csv";
//
//
//		ValidatableResponse response = setupJwtCall(given(),  user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
//										   .contentType(ContentType.MULTIPART)
//										   .multiPart("file", csvData)
//										   .multiPart("fileName", "test.csv")
//										   .when()
//										   .post("", DEFAULT_TEST_DB_NAME)
//										   .then();
//
//		response.statusCode(Response.Status.OK.getStatusCode());
//
//		log.info("Got response body: {}", response.extract().body().asString());
//	}
}