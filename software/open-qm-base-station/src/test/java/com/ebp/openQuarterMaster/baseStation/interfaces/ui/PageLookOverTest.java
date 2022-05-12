package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
			Arguments.of(true,  "/images", "Images"),
			Arguments.of(true,  "/storage", "Storage"),
			Arguments.of(true,  "/items", "Items")
		);
	}
	
	@Inject
	TestUserService testUserService;
	
	@Inject
	WebDriverWrapper webDriverWrapper;
	
	/**
	 * Sanity-check type test to prove all pages load
	 */
	@ParameterizedTest
	@MethodSource("getPages")
	public void testPages(boolean loggedIn, String endpoint, String expectedTitle) {
		User testUser = this.testUserService.getTestUser(true, true);
		
		if(loggedIn){
			log.info("Logging in user.");
			this.webDriverWrapper.loginUser(testUser);
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
