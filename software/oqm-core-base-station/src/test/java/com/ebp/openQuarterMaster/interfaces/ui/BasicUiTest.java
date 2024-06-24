package com.ebp.openQuarterMaster.interfaces.ui;

import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.testResources.ui.WebDriverWrapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class BasicUiTest extends WebUiTest {

	@Test
	public void testPageOverview() {
		// TODO:: need to tell keycloak devservice to use testcontainer hostname
		this.getWebDriverWrapper().goTo("");
	}

	//TODO:: page lookover test
}
