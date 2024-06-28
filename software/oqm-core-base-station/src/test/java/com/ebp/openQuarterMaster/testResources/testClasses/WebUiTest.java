package com.ebp.openQuarterMaster.testResources.testClasses;

import com.ebp.openQuarterMaster.testResources.testUsers.TestUser;
import com.ebp.openQuarterMaster.testResources.testUsers.TestUserService;
import com.ebp.openQuarterMaster.testResources.ui.PlaywrightSetup;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkus.test.common.http.TestHTTPResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {

	private static Path getCurTestDir(TestInfo testInfo){
		log.debug("Display name: {}", testInfo.getDisplayName());

		Path output = PlaywrightSetup.RECORDINGS_DIR.resolve(testInfo.getTestClass().get().getName());

		if(testInfo.getDisplayName().startsWith("[")){
			return output.resolve(testInfo.getTestMethod().get().getName())
				.resolve(testInfo.getDisplayName().replaceAll("/", ""));
		} else {
			return output.resolve(testInfo.getDisplayName().replaceAll("\\(\\)", ""));
		}
	}

	protected static Page.ScreenshotOptions getScreenshotOptions() {
		return new Page.ScreenshotOptions().setFullPage(true);
	}

	@Getter
	@TestHTTPResource("/")
	URL index;

	@Getter
	private BrowserContext context;

	@Getter
	private Path curTestUiResultDir;

	@Getter
	private final TestUserService testUserService = TestUserService.getInstance();

	@BeforeEach
	public void beforeEachUi(TestInfo testInfo) {
		this.curTestUiResultDir = getCurTestDir(testInfo);
		Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();

		newContextOptions.setRecordVideoDir(this.curTestUiResultDir);

		this.context = PlaywrightSetup.getINSTANCE().getBrowser().newContext(newContextOptions);
	}

	@AfterEach
	public void afterEachUi(TestInfo testInfo) throws InterruptedException, IOException {

		for(int i = 0; i < this.getContext().pages().size(); i++){
			Page curPage = this.getContext().pages().get(i);
			Path curPageFinalScreenshot = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-final.png");
			Path curPageHtmlFile = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-final-code.html");

			curPage.screenshot(getScreenshotOptions().setPath(curPageFinalScreenshot));
			try(OutputStream outputStream = new FileOutputStream(curPageHtmlFile.toFile())){
				outputStream.write(curPage.content().getBytes());
			}
		}
		Thread.sleep(1_500);
		this.context.close();
	}

	protected Page getLoggedInPage(TestUser user, String page){
		Page output = this.getContext().newPage();
		Response response = output.navigate(this.getIndex().toString() + page);

		assertEquals("OK", response.statusText());
		output.waitForLoadState();

		//TODO:: determine if logged in. If not, login with test user.

		return output;
	}
}
