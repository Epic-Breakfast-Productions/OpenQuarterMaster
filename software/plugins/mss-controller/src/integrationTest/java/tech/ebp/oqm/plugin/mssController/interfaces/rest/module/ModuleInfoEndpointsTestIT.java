package tech.ebp.oqm.plugin.mssController.interfaces.rest.module;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;

@QuarkusIntegrationTest
@QuarkusTestResource(
	value = TestModuleResource.class,
	restrictToAnnotatedClass = true,
	initArgs = {
		@ResourceArg(name = TestModuleResource.NUM_SERIAL_MODULE_RES_NAME, value = "1")
	}
)
public class ModuleInfoEndpointsTestIT extends ModuleInfoEndpointsTest {
}
