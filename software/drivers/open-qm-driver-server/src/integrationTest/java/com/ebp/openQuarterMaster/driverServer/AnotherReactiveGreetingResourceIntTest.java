package com.ebp.openQuarterMaster.driverServer;

import com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		        @ResourceArg(name = "randomArg", value = "2")
	},
						restrictToAnnotatedClass = true
)
//@QuarkusIntegrationTest
@Slf4j
public class AnotherReactiveGreetingResourceIntTest {

	@Test
	public void noopTest(){

	}

	@TestFactory
	List<DynamicNode> getTests() {
		return Arrays.asList(
			DynamicTest.dynamicTest("Add test",
									() -> assertEquals(2, Math.addExact(1, 1))),
			DynamicTest.dynamicTest("Multiply Test",
									() -> assertEquals(4, Math.multiplyExact(2, 2))));
	}
}
