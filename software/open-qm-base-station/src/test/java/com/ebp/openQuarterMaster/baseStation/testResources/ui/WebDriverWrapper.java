package com.ebp.openQuarterMaster.baseStation.testResources.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.UserRelated;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.KeycloakLogin;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import tech.ebp.oqm.lib.core.object.user.User;

import java.time.Duration;
import java.util.List;

import static com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService.TEST_PASSWORD_ATT_KEY;
import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

@Slf4j
public class WebDriverWrapper {
	
	@Getter
	private WebDriver webDriver = null;
	
	private final int defaultWait = ConfigProvider.getConfig().getValue("test.selenium.defaultWait", Integer.class);
	private final String baseUrl = ConfigProvider.getConfig().getValue("runningInfo.baseUrl", String.class);
	private final String keycloakInteractionBase = ConfigProvider.getConfig().getValue("service.externalAuth.interactionBase", String.class);
	private final AuthMode authMode = ConfigProvider.getConfig().getValue("service.authMode", AuthMode.class);
	
	public WebDriverWrapper(){
		this.webDriver = TestResourceLifecycleManager.getWebDriver();
	}
	
	public void cleanup() {
		log.info("Cleaning up browser after test.");
		
		WebDriver driver = getWebDriver();
		log.info(
			"Last Page: \"{}\" {}",
			driver.getTitle(),
			driver.getCurrentUrl()
		);
		log.debug("Last Page html: \n{}", driver.getPageSource());
		
		if (EXTERNAL.equals(this.authMode)) {
			String logoutUrl = this.keycloakInteractionBase + "/logout";
			log.info("Logging out of Keycloak at: {}", logoutUrl);
			driver.get(logoutUrl);
			driver.manage().deleteAllCookies();
		}
		this.goToIndex();
		driver.manage().deleteAllCookies();
		driver.get("about:logo");
		driver.navigate().refresh();
		
		log.info("Completed cleanup of webdriver wrapper.");
	}
	
	public WebElement findElement(By by) {
		return this.getWebDriver().findElement(by);
	}
	
	public List<WebElement> findElements(By by) {
		return this.getWebDriver().findElements(by);
	}
	
	public void goTo(String endpoint) {
		this.getWebDriver().get(this.baseUrl + endpoint);
	}
	
	public void goToIndex() {
		this.goTo("");
	}
	
	public WebDriverWait getWait(int seconds) {
		return new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(seconds));
	}
	
	public WebDriverWait getWait() {
		return this.getWait(this.defaultWait);
	}
	
	public WebElement waitFor(WebDriverWait wait, By by) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	public WebElement waitFor(By by) {
		return this.waitFor(this.getWait(), by);
	}
	
	public void waitForPageLoad() {
		this.waitFor(By.id("footer"));
		log.info("Page loaded: {}", this.getWebDriver().getCurrentUrl());
	}
	
	public void loginUser(User testUser) {
		log.info("Logging in user {}.", testUser.getUsername());
		this.goToIndex();
		
		this.waitForPageLoad();
		
		if (EXTERNAL.equals(this.authMode)) {
			log.info("Logging in via external means.");
			this.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
			
			log.info("Went to keycloak at: {}", this.getWebDriver().getCurrentUrl());
			
			this.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
			this.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
			
			this.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		} else {
			log.info("Logging in via self.");
			this.getWebDriver().findElement(Root.EMAIL_USERNAME_INPUT).sendKeys(testUser.getUsername());
			this.getWebDriver()
				.findElement(Root.PASSWORD_INPUT)
				.sendKeys(testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY));
			log.info("Entered user's credentials.");
			this.getWebDriver().findElement(Root.SIGN_IN_BUTTON).click();
			log.info("Clicked the sign in button.");
		}
		this.waitForPageLoad();
		//TODO:: log page messages
		
		UserRelated.assertUserLoggedIn(this, testUser);
		
		log.info("Logged in user.");
	}
}
