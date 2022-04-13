package com.ebp.openQuarterMaster.driverServer;

import com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		//        @ResourceArg(name = TestResourceLifecycleManager.NUM_SERIAL_PORTS_ARG, value = "2")
	}
)
@QuarkusIntegrationTest
public class ReactiveGreetingResourceIntTest extends ReactiveGreetingResourceTest {

}
