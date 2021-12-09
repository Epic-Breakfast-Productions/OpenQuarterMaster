package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.profiles.ExternalAuthTestProfile;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.General;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.KeycloakLogin;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService.TEST_PASSWORD_ATT_KEY;
import static com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.LocationAssertions.assertOnPage;
import static com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.UserRelated.assertUserAdminLoggedIn;
import static com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.UserRelated.assertUserLoggedIn;

@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(value = TestResourceLifecycleManager.class, initArgs = @ResourceArg(name=TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value="true"), restrictToAnnotatedClass = true)
public class LoginExternalTest extends WebUiTest {
    @Inject
    TestUserService testUserService;

    @Inject
    WebDriverWrapper webDriverWrapper;

    @Test
    public void testLogin() throws InterruptedException {
        User testUser = this.testUserService.getTestUser(false, true);
        this.webDriverWrapper.goToIndex();

        this.webDriverWrapper.waitForPageLoad();

        this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();

        log.info("Went to keycloak at: {}", this.webDriverWrapper.getWebDriver().getCurrentUrl());

        this.webDriverWrapper.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
        this.webDriverWrapper.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));

        this.webDriverWrapper.findElement(KeycloakLogin.LOGIN_BUTTON).click();

        this.webDriverWrapper.waitForPageLoad();
        assertUserLoggedIn(this.webDriverWrapper, testUser);
    }

    @Test
    public void testLoginWithToken() throws InterruptedException {
        User testUser = this.testUserService.getTestUser(false, true);
        String userJwt = this.testUserService.getTestUserToken(testUser);
        this.webDriverWrapper.goToIndex();

        this.webDriverWrapper.waitForPageLoad();

        this.webDriverWrapper.findElement(Root.JWT_INPUT).sendKeys(userJwt);
        this.webDriverWrapper.findElement(Root.SIGN_IN_BUTTON).click();
        this.webDriverWrapper.waitForPageLoad();

        assertUserLoggedIn(this.webDriverWrapper, testUser);
    }

    @Test
    public void testLoginAdminWithToken() throws InterruptedException {
        User testUser = this.testUserService.getTestUser(true, true);

        this.webDriverWrapper.loginUser(testUser);

        assertUserAdminLoggedIn(this.webDriverWrapper, testUser);
    }

    @Test
    public void testLogout() throws InterruptedException {
        User testUser = this.testUserService.getTestUser(true, true);

        this.webDriverWrapper.loginUser(testUser);

        this.webDriverWrapper.waitFor(General.USERNAME_DISPLAY).click();
        this.webDriverWrapper.waitFor(General.LOGOUT_BUTTON).click();

        this.webDriverWrapper.waitFor(Root.SIGN_IN_BUTTON);

        assertOnPage(this.webDriverWrapper, "/");
    }
}
