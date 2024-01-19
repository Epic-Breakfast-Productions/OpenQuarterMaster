package tech.ebp.oqm.baseStation.interfaces.ui;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.WebUiTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusIntegrationTest
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.UI_TEST_ARG, value = "true"),
		@ResourceArg(name = TestResourceLifecycleManager.INT_TEST_ARG, value = "true")
	},
	restrictToAnnotatedClass = true
)
public class PageLookOverIntTest extends WebUiTest {
	
	public static Stream<Arguments> getPages(){
		return Stream.of(
			Arguments.of(false, "/", "Login"),
			Arguments.of(false,  "/help", "Help and User Guide"),
			Arguments.of(true,  "/overview", "Overview"),
			Arguments.of(true,  "/help", "Help and User Guide"),
			Arguments.of(true,  "/codes", "Create QR & Bar Codes"),
			Arguments.of(true,  "/you", "Your Profile"),
			Arguments.of(true,  "/inventoryAdmin", "Inventory Administration"),
			Arguments.of(true,  "/userAdmin", "User Administration"),
			Arguments.of(true,  "/images", "Images"),
			Arguments.of(true,  "/storage", "Storage"),
			Arguments.of(true,  "/items", "Items"),
			Arguments.of(true,  "/categories", "Categories"),
			Arguments.of(true,  "/itemLists", "Item Lists"),
			Arguments.of(true,  "/itemCheckout", "Item Checkout"),
			Arguments.of(true,  "/entityView/baseStation", "Base Station")
			//			Arguments.of(true,  "/entityView/type/id", "Entity") //TODO
		);
	}
	
	TestUserService testUserService = new TestUserService();
	
	/**
	 * Sanity-check type test to prove all pages load
	 */
	@ParameterizedTest
	@MethodSource("getPages")
	public void testPages(boolean loggedIn, String endpoint, String expectedTitle) {
		User testUser = this.testUserService.getTestUser();
		
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
