package tech.ebp.oqm.baseStation.interfaces.ui;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;

@QuarkusIntegrationTest
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.UI_TEST_ARG, value = "true"),
		@ResourceArg(name = TestResourceLifecycleManager.INT_TEST_ARG, value = "true")
	},
	restrictToAnnotatedClass = true
)
public class PageLookOverIntTest extends PageLookOverTest {

}
