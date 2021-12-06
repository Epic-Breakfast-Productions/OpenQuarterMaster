package com.ebp.openQuarterMaster.baseStation.testResources.ui;

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
import javax.enterprise.context.RequestScoped;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;

@Slf4j
@RequestScoped
public class WebDriverWrapper implements Closeable {
    @Getter
    private volatile WebDriver webDriver;

    static {
        WebDriverManager.firefoxdriver().setup();
    }

    @ConfigProperty(name = "test.selenium.headless", defaultValue = "true")
    boolean headless;
    @ConfigProperty(name="test.selenium.defaultWait", defaultValue = "5")
    int defaultWait;
    @ConfigProperty(name="runningInfo.baseUrl")
    String baseUrl;

    @PostConstruct
    void setup(){
        this.webDriver = new FirefoxDriver(new FirefoxOptions().setHeadless(headless));
    }

    @PreDestroy
    public void close(){
        this.webDriver.close();
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
        return new WebDriverWait(this.webDriver, Duration.ofSeconds(seconds));
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
}
