package com.ebp.openQuarterMaster.baseStation.testResources.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
//@RequestScoped
@ApplicationScoped
public class WebDriverWrapper implements Closeable {
    static {
        WebDriverManager.firefoxdriver().setup();
    }

    @Getter
    private volatile WebDriver webDriver = null;

    @ConfigProperty(name = "test.selenium.headless", defaultValue = "true")
    boolean headless;
    @ConfigProperty(name = "test.selenium.quickClean", defaultValue = "true")
    boolean quickClean;
    @ConfigProperty(name="test.selenium.defaultWait", defaultValue = "5")
    int defaultWait;
    @ConfigProperty(name="runningInfo.baseUrl")
    String baseUrl;
    @Inject
    TestUserService testUserService;

    @PostConstruct
    void setup(){
        log.info("Creating new web driver.");
        this.webDriver = new FirefoxDriver(new FirefoxOptions().setHeadless(headless));
    }

    @PreDestroy
    public void close(){
        log.info("Closing out web driver.");
        this.webDriver.close();
    }

    public void cleanup(){
        log.info("Cleaning up browser after test.");

        if(this.quickClean) {
            this.webDriver.manage().deleteAllCookies();
            this.webDriver.get("about:logo");
            this.webDriver.navigate().refresh();
        } else {
//            this.webDriver.quit();
            this.webDriver.close();
            this.webDriver = null;
            this.setup();
        }
    }

    public WebElement findElement(By by){
        return this.getWebDriver().findElement(by);
    }
    public List<WebElement> findElements(By by){
        return this.getWebDriver().findElements(by);
    }

    public void goTo(String endpoint){
        this.getWebDriver().get(this.baseUrl + endpoint);
    }

    public void goToIndex(){
        this.goTo("");
    }

    public WebDriverWait getWait(int seconds){
        return new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(seconds));
    }

    public WebDriverWait getWait(){
        return this.getWait(this.defaultWait);
    }

    public WebElement waitFor(WebDriverWait wait, By by){
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public WebElement waitFor(By by){
        return this.waitFor(this.getWait(), by);
    }

    public void waitForPageLoad(){
        this.waitFor(By.id("footer"));
        log.info("Page loaded: {}", this.getWebDriver().getCurrentUrl());
    }

    public void loginUser(User testUser){
        this.goToIndex();

        this.waitForPageLoad();

        this.getWebDriver().findElement(Root.JWT_INPUT).sendKeys(this.testUserService.getTestUserToken(testUser));
        this.getWebDriver().findElement(Root.SIGN_IN_BUTTON).click();
        this.waitForPageLoad();
    }
}
