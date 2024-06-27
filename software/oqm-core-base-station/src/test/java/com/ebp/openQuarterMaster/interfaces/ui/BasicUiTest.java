package com.ebp.openQuarterMaster.interfaces.ui;

import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.testResources.ui.WebDriverWrapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
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
public class BasicUiTest extends WebUiTest {

	@TestHTTPResource("/")
	URL index;

	@Test
	public void testPageOverview() {
		final Page page = this.getContext().newPage();
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
