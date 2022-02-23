package com.ebp.openQuarterMaster.baseStation.testResources.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

@Slf4j
//@RequestScoped
@ApplicationScoped
public class WebDriverWrapper implements Closeable {
	
	private static WebDriver WEB_DRIVER = null;
	private static final ReentrantLock DRIVER_SEMAPHORE = new ReentrantLock();
	
	public static void initDriver() {
		DRIVER_SEMAPHORE.lock();
		try {
			if (WEB_DRIVER == null) {
				log.info("Setting up new firefox window.");
				StopWatch sw = StopWatch.createStarted();
				WEB_DRIVER = new FirefoxDriver(new FirefoxOptions().setHeadless(
					ConfigProvider.getConfig().getValue("test.selenium.headless", Boolean.class)
				));
				WEB_DRIVER.get("about:logo");
				sw.stop();
				log.info("DONE setting up firefox window in: {}", sw);
			}
		} catch(Throwable e) {
			log.error("FAILED to set up new firefox window: ", e);
		} finally {
			DRIVER_SEMAPHORE.unlock();
		}
	}
	
	public static WebDriver getStaticWebDriver() {
		DRIVER_SEMAPHORE.lock();
		try {
			if (WEB_DRIVER == null) {
				initDriver();
			}
			return WEB_DRIVER;
		} finally {
			DRIVER_SEMAPHORE.unlock();
		}
	}
	
	static {
		WebDriverManager.firefoxdriver().setup();
		
		//TODO:: init web driver at start of tests rather than halfway through
	}
	
	public WebDriver getWebDriver() {
		return getStaticWebDriver();
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
		log.info("Creating new web driver.");
		initDriver();
	}
	
	@PreDestroy
	public void close() {
		log.info("Closing out web driver.");
		DRIVER_SEMAPHORE.lock();
		try {
			getWebDriver().quit();
			WEB_DRIVER = null;
		} catch(Throwable e){
			log.error("Failed to close web driver: ", e);
		} finally {
			DRIVER_SEMAPHORE.unlock();
		}
	}
	
	public void cleanup() {
		log.info("Cleaning up browser after test.");
		
		DRIVER_SEMAPHORE.lock();
		try {
			WebDriver driver = getWebDriver();
			if (this.quickClean) {
				if(EXTERNAL.equals(this.authMode)){
					driver.get(this.keycloakInteractionBase + "/logout");
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
		} finally {
			DRIVER_SEMAPHORE.unlock();
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
		
		if(EXTERNAL.equals(this.authMode)) {
			this.getWebDriver().findElement(Root.JWT_INPUT).sendKeys(this.testUserService.getTestUserToken(testUser));
		} else {
			this.getWebDriver().findElement(Root.EMAIL_USERNAME_INPUT).sendKeys(testUser.getUsername());
			this.getWebDriver().findElement(Root.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY));
		}
		this.getWebDriver().findElement(Root.SIGN_IN_BUTTON).click();
		this.waitForPageLoad();
	}
}
