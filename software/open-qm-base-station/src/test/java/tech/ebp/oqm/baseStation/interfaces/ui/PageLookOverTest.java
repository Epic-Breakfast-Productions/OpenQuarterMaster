package tech.ebp.oqm.baseStation.interfaces.ui;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.lib.core.object.user.User;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dumb/quick test to ensure all pages can at least load.
 */
@Tag("integration")
@Tag("sanity")
@Slf4j
@QuarkusTest
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.UI_TEST_ARG, value = "true")
	},
	restrictToAnnotatedClass = true
)
public class PageLookOverTest extends WebUiTest {
	
	public static Stream<Arguments> getPages(){
		return Stream.of(
			Arguments.of(false, "/", "Login"),
			Arguments.of(false, "/accountCreate", "Account Create"),
			Arguments.of(true,  "/overview", "Overview"),
			Arguments.of(true,  "/help", "Help and User Guide"),
			Arguments.of(true,  "/codes", "Create QR & Bar Codes"),
			Arguments.of(true,  "/you", "Your Profile"),
			Arguments.of(true,  "/inventoryAdmin", "Inventory Administration"),
			Arguments.of(true,  "/images", "Images"),
			Arguments.of(true,  "/storage", "Storage"),
			Arguments.of(true,  "/items", "Items")
		);
	}
	
	TestUserService testUserService = new TestUserService();
	
	/**
	 * Sanity-check type test to prove all pages load
	 */
	@ParameterizedTest
	@MethodSource("getPages")
	public void testPages(boolean loggedIn, String endpoint, String expectedTitle) {
		User testUser = this.testUserService.getTestUser(true, true);
		
		if(loggedIn){
			this.webDriverWrapper.loginUser(testUser);
			log.info("User logged in.");
		}
		
		log.info("Attempting page: {}", endpoint);
		this.webDriverWrapper.goTo(endpoint);
		this.webDriverWrapper.waitForPageLoad();
		
		String curUrl = this.webDriverWrapper.getWebDriver().getCurrentUrl();
		assertTrue(curUrl.endsWith(endpoint), "Not on expected page; actually on: " + curUrl);
		
		String curTitle = this.webDriverWrapper.getWebDriver().getTitle();
		assertTrue(curTitle.contains(expectedTitle), "Title was unexpected: " + curTitle);
		
		log.debug("HTML: {}", this.webDriverWrapper.getWebDriver().getPageSource());
	}
}
