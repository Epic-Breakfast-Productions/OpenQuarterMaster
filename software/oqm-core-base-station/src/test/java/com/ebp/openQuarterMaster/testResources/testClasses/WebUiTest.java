package com.ebp.openQuarterMaster.testResources.testClasses;

import com.ebp.openQuarterMaster.testResources.ui.PlaywrightSetup;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {

	@Getter
	private BrowserContext context;

	@BeforeEach
	public void beforeEachUi(TestInfo testInfo) throws InterruptedException {
		Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();

		newContextOptions.setRecordVideoDir(
			PlaywrightSetup.RECORDINGS_DIR
				.resolve(testInfo.getTestClass().get().getName())
				.resolve(testInfo.getDisplayName())
		);

		this.context = PlaywrightSetup.getINSTANCE().getBrowser().newContext(newContextOptions);
	}

	@AfterEach
	public void afterEachUi(TestInfo testInfo) throws InterruptedException {
		Thread.sleep(5_000);
		this.context.close();
	}
}
