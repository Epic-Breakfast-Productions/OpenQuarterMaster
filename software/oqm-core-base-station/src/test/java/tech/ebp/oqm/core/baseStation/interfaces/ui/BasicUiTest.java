package tech.ebp.oqm.core.baseStation.interfaces.ui;

import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
public class BasicUiTest extends WebUiTest {

	@Test
	public void testPageOverview() {
		this.getLoggedInPage(this.getTestUserService().getTestUser(), "/");

	}
}
