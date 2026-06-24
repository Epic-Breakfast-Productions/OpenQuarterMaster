package tech.ebp.oqm.plugin.mssController.interfaces;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(
	value = TestModuleResource.class,
	restrictToAnnotatedClass = true,
	initArgs = {
		@ResourceArg(name = TestModuleResource.NUM_SERIAL_MODULE_RES_NAME, value = "1")
	}
)
class GreetingResourceTest {

	@Test
	public void testHelloEndpoint() {
		assertTrue(true);
	}

}
