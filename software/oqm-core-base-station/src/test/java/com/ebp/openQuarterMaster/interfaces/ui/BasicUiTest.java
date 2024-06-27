package com.ebp.openQuarterMaster.interfaces.ui;

import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.testResources.ui.WebDriverWrapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@WithPlaywright()
//@QuarkusTestResource(TestResourceLifecycleManager.class)
public class BasicUiTest extends WebUiTest {

	@InjectPlaywright
	BrowserContext context;

	@TestHTTPResource("/")
	URL index;

	//TODO:: need to figure out how to deal with hostnames in docker
	@Test
	public void testPageOverview() throws InterruptedException {
		final Page page = context.newPage();
		Response response = page.navigate(index.toString());
		assertEquals("OK", response.statusText());

		page.waitForLoadState();
		page.screenshot();
		page.screenshot(new Page.ScreenshotOptions()
			.setPath(Paths.get("screenshot.png"))
			.setFullPage(true));




//		Thread.sleep(5*60*1000);
		// TODO:: need to tell keycloak devservice to use testcontainer hostname
//		this.getWebDriverWrapper().goTo("");
	}

	//TODO:: page lookover test
}
