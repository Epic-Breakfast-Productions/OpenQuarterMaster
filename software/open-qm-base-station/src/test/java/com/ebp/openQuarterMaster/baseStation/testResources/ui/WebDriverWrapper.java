package com.ebp.openQuarterMaster.baseStation.testResources.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
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
	@ConfigProperty(name = "test.selenium.hostIp", defaultValue = "localhost")
	String hostIp;
	@ConfigProperty(name = "runningInfo.port")
	int port;
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
		
		if (this.quickClean) {
			if(AuthMode.EXTERNAL.equals(this.authMode)){
				driver.get(this.keycloakInteractionBase + "/logout");
				driver.manage().deleteAllCookies();
			}
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
		
		this.getWebDriver().findElement(Root.JWT_INPUT).sendKeys(this.testUserService.getTestUserToken(testUser));
		this.getWebDriver().findElement(Root.SIGN_IN_BUTTON).click();
		this.waitForPageLoad();
	}
}
