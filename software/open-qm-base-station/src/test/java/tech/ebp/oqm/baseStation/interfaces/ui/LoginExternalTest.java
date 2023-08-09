package tech.ebp.oqm.baseStation.interfaces.ui;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.profiles.ExternalAuthTestProfile;
import tech.ebp.oqm.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.baseStation.testResources.ui.assertions.LocationAssertions;
import tech.ebp.oqm.baseStation.testResources.ui.assertions.UserRelated;
import tech.ebp.oqm.baseStation.testResources.ui.pages.General;
import tech.ebp.oqm.baseStation.testResources.ui.pages.KeycloakLogin;
import tech.ebp.oqm.baseStation.testResources.ui.pages.Root;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;

import jakarta.inject.Inject;

import static com.mongodb.assertions.Assertions.assertTrue;

@Tag("integration")
@Tag("externalAuth")
@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value = "true"),
		@ResourceArg(name = TestResourceLifecycleManager.UI_TEST_ARG, value = "true")
	},
	restrictToAnnotatedClass = true
)
public class LoginExternalTest extends WebUiTest {
	
	@Inject
	TestUserService testUserService;
	
	@Test
	public void testLogin() {
		User testUser = this.testUserService.getTestUser(false, true);
		this.webDriverWrapper.goToIndex();
		
		this.webDriverWrapper.waitForPageLoad();
		
		this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
		
		log.info("Went to keycloak at: {}", this.webDriverWrapper.getWebDriver().getCurrentUrl());
		
		this.webDriverWrapper.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
		this.webDriverWrapper.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY));
		
		this.webDriverWrapper.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		
		this.webDriverWrapper.waitForPageLoad();
		UserRelated.assertUserLoggedIn(this.webDriverWrapper, testUser);
		LocationAssertions.assertOnPage(this.webDriverWrapper, "/overview");
		
		//test refresh keys
		this.webDriverWrapper.getWebDriver().navigate().refresh();
		
		this.webDriverWrapper.waitForPageLoad();
		UserRelated.assertUserLoggedIn(this.webDriverWrapper, testUser);
		
	}
	
	@Test
	public void testLoginWithReturnPath() {
		User testUser = this.testUserService.getTestUser(false, true);
		String queryPath = "/storage?label=some&pageNum=1";
		
		this.webDriverWrapper.goTo(queryPath);
		
		this.webDriverWrapper.waitForPageLoad();
		
		log.info(
			"Login link url: {}",
			this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).getAttribute("href")
		);
		
		this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
		
		this.webDriverWrapper.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
		log.info("Went to keycloak at: {}", this.webDriverWrapper.getWebDriver().getCurrentUrl());
		this.webDriverWrapper.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY));
		
		this.webDriverWrapper.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		
		this.webDriverWrapper.waitForPageLoad();
		UserRelated.assertUserLoggedIn(this.webDriverWrapper, testUser);
		
		LocationAssertions.assertOnPage(this.webDriverWrapper, "/storage");
		assertTrue(this.webDriverWrapper.getWebDriver().getCurrentUrl().endsWith(queryPath));
	}
	
	@Test
	public void testLogout() {
		User testUser = this.testUserService.getTestUser(true, true);
		
		this.webDriverWrapper.loginUser(testUser);
		
		this.webDriverWrapper.waitFor(General.USERNAME_DISPLAY).click();
		this.webDriverWrapper.waitFor(General.LOGOUT_BUTTON).click();
		
		this.webDriverWrapper.waitFor(Root.LOGIN_WITH_EXTERNAL_LINK);
		
		LocationAssertions.assertOnPage(this.webDriverWrapper, "/");
		
		//TODO:: assert message saying logged out
	}
}
