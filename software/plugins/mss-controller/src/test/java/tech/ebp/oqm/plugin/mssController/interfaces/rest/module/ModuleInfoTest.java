package tech.ebp.oqm.plugin.mssController.interfaces.rest.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.MssConnectorInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;
import tech.ebp.oqm.plugin.mssController.testResources.testClasses.RunningServerTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(
	value = TestModuleResource.class,
	restrictToAnnotatedClass = true,
	initArgs = {
		@ResourceArg(name = TestModuleResource.NUM_SERIAL_MODULE_RES_NAME, value = "1")
	}
)
class ModuleInfoTest extends RunningServerTest {

	@Test
	public void testGetModules() throws JsonProcessingException {
		MssConnectorInfo info;
		{ //initial list
			ArrayNode gotten = given()
								   .when()
								   .get("/module")
								   .then()
								   .statusCode(200)
								   .extract().body().as(ArrayNode.class);

			log.info("Got connection data list: {}", gotten);

			List<MssConnectorInfo> list = JacksonUtils.OBJECT_MAPPER.treeToValue(
				gotten, new TypeReference<List<MssConnectorInfo>>() {
				}
			);

			log.info("Deserialized info list: {}", list);

			assertEquals(1, list.size());
			info = list.getFirst();
		}
		{ //get info
			MssConnectorInfo gotten = given()
										  .when()
										  .pathParams("serialId", info.getSerialId())
										  .get("/module/{serialId}")
										  .then()
										  .statusCode(200)
										  .extract().body().as(MssConnectorInfo.class);
			log.info("Got specific info: {}", gotten);
			assertEquals(info, gotten);
		}
		{ //get state
			ModuleState gotten = given()
										  .when()
										  .pathParams("serialId", info.getSerialId())
										  .get("/module/{serialId}/state")
										  .then()
										  .statusCode(200)
										  .extract().body().as(ModuleState.class);
			log.info("Got module state: {}", gotten);
		}

	}
}
