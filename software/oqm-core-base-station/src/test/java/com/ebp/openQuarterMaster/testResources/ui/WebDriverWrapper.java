package com.ebp.openQuarterMaster.testResources.ui;

import com.ebp.openQuarterMaster.testResources.testUsers.TestUser;
import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.testResources.ui.assertions.UserRelated;
import com.ebp.openQuarterMaster.testResources.ui.pages.General;
import com.ebp.openQuarterMaster.testResources.ui.pages.KeycloakLogin;
import com.ebp.openQuarterMaster.testResources.ui.pages.Root;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Slf4j
public class WebDriverWrapper {
	
	@Getter
	private WebDriver webDriver = null;
	
	private final int defaultWait = ConfigProvider.getConfig().getValue("test.selenium.defaultWait", Integer.class);
	private final String baseUrl = ConfigProvider.getConfig().getValue("runningInfo.baseUrl", String.class);
	private final String keycloakInteractionBase = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String.class);
	
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
		
		if(isLoggedIn()) {
//			this.logoutUser();
		}
		//http://auth-server/auth/realms/{realm-name}/protocol/openid-connect/logout?redirect_uri=encodedRedirectUri
//		String logoutUrl = this.keycloakInteractionBase + "/protocol/openid-connect/logout";
//		log.info("Logging out of Keycloak at: {}", logoutUrl);
//		driver.manage().deleteAllCookies();
//		driver.get(logoutUrl);
		
		driver.manage().deleteAllCookies();
		this.goToIndex();
		driver.manage().deleteAllCookies();
		driver.get("about:logo");
		driver.manage().deleteAllCookies();
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
	
	public boolean isLoggedIn() {
		try {
			this.getWebDriver().findElement(General.USERNAME_DISPLAY);
			return true;
		} catch(NoSuchElementException e){
			return false;
		}
	}
	
	public void loginUser(TestUser testUser) {
		log.info("Logging in user {}.", testUser.getUsername());
		this.goToIndex();

		this.waitForPageLoad();

		log.info("Logging in via external means.");
		this.getWebDriver().findElement(Root.CONTINUE_LINK).click();

		log.info("Went to keycloak at: {}", this.getWebDriver().getCurrentUrl());

		this.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
		this.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getPassword());

		this.findElement(KeycloakLogin.LOGIN_BUTTON).click();

		this.waitForPageLoad();
		//TODO:: log page messages

		UserRelated.assertUserLoggedIn(this, testUser);

		log.info("Logged in user.");
	}

	public void logoutUser() {
		this.getWebDriver().findElement(General.USERNAME_DISPLAY).click();
		this.getWebDriver().findElement(General.LOGOUT_BUTTON).click();
		this.waitForPageLoad();
	}
}
