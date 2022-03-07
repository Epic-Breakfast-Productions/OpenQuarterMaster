package com.ebp.openQuarterMaster.baseStation.testResources.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.KeycloakLogin;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;

import static com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService.TEST_PASSWORD_ATT_KEY;
import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

@Slf4j
//@RequestScoped
@ApplicationScoped
public class WebDriverWrapper implements Closeable {
	
	static {
		//		WebDriverManager.firefoxdriver().setup();
		
		//TODO:: init web driver at start of tests rather than halfway through
	}
	
	private WebDriver driver = null;
	
	public WebDriver getWebDriver() {
		return this.driver;
	}
	
	@ConfigProperty(name = "test.selenium.headless", defaultValue = "true")
	boolean headless;
	@ConfigProperty(name = "test.selenium.quickClean", defaultValue = "true")
	boolean quickClean;
	@ConfigProperty(name = "test.selenium.defaultWait", defaultValue = "5")
	int defaultWait;
	@ConfigProperty(name = "runningInfo.baseUrl")
	String baseUrl;
	@ConfigProperty(name = "service.externalAuth.interactionBase")
	String keycloakInteractionBase;
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
	@Inject
	TestUserService testUserService;
	
	@PostConstruct
	void setup() {
		this.driver = TestResourceLifecycleManager.getWebDriver();
	}
	
	@PreDestroy
	public void close() {
		log.info("Closing out web driver.");
		getWebDriver().quit();
		this.driver = null;
	}
	
	public void cleanup() {
		log.info("Cleaning up browser after test.");
		
		WebDriver driver = getWebDriver();
		log.info(
			"Last Page: \"{}\" {}",
			driver.getTitle(),
			driver.getCurrentUrl()
		);
		log.debug("page html: \n{}", driver.getPageSource());
		
		if (this.quickClean) {
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
		} else {
			this.close();
			this.setup();
		}
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
		this.goToIndex();
		
		this.waitForPageLoad();
		
		if (EXTERNAL.equals(this.authMode)) {
			this.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
			
			log.info("Went to keycloak at: {}", this.getWebDriver().getCurrentUrl());
			
			this.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
			this.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
			
			this.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		} else {
			this.getWebDriver().findElement(Root.EMAIL_USERNAME_INPUT).sendKeys(testUser.getUsername());
			this.getWebDriver()
				.findElement(Root.PASSWORD_INPUT)
				.sendKeys(testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY));
			log.info("Entered user's credentials.");
			this.getWebDriver().findElement(Root.SIGN_IN_BUTTON).click();
			log.info("Clicked the sign in button.");
		}
		this.waitForPageLoad();
	}
}
