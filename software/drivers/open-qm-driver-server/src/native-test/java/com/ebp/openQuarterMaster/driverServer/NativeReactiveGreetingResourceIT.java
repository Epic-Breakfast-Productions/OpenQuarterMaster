package com.ebp.openQuarterMaster.driverServer;

import com.ebp.openQuarterMaster.driverServer.ReactiveGreetingResourceTest;
import com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		//        @ResourceArg(name = TestResourceLifecycleManager.NUM_SERIAL_PORTS_ARG, value = "2")
	}
)
public class NativeReactiveGreetingResourceIT extends ReactiveGreetingResourceTest {

    // Execute the same tests but in native mode.
}