package tech.ebp.oqm.plugin.mssController.interfaces.ui;

import tech.ebp.oqm.plugin.mssController.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.plugin.mssController.testResources.testUsers.TestUserService;
import io.quarkus.test.junit.QuarkusTest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
public class BasicUiTest extends WebUiTest {

	@Getter
	private final TestUserService testUserService = TestUserService.getInstance();

	@Test
	public void testPageOverview() {
		this.getLoggedInPage(this.getTestUserService().getTestUser(), "/");

//		Thread.sleep(5*60*1000);
		// TODO:: need to tell keycloak devservice to use testcontainer hostname
//		this.getWebDriverWrapper().goTo("");
	}

	//TODO:: page lookover test
}
