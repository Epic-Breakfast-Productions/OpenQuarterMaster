package tech.ebp.oqm.core.baseStation.testResources.testClasses;

import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUser;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUserService;
import tech.ebp.oqm.core.baseStation.testResources.ui.PlaywrightSetup;
import com.microsoft.playwright.*;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions()
			.setRecordVideoDir(this.curTestUiResultDir)
			.setScreenSize(1920,1080)
			.setRecordVideoSize(1920,1080)
			.setViewportSize(1920,1080)
			;

		this.context = PlaywrightSetup.getInstance().getBrowser().newContext(newContextOptions);
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
		Thread.sleep(250);
		this.context.close();
	}

	protected Page getLoggedInPage(TestUser user, String page){
		Page output = this.getContext().newPage();

		if(page.startsWith("/")){
			page = page.substring(1);
		}

		String url = this.getIndex().toString() + page;
		log.info("Navigating to {}", url);
		Response response = output.navigate(url);

		assertEquals("OK", response.statusText());
		output.waitForLoadState();

		if(output.title().contains("Sign in to Open QuarterMaster")){
			log.info("Need to log in user.");

			Locator locator = output.locator("#password");
			locator.fill(user.getPassword());
			locator = output.locator("#username");
			locator.fill(user.getUsername());

			output.locator("#kc-login").click();
			output.waitForLoadState();

			if(!output.locator("#topOqmLogo").isVisible()){
				if(!output.getByText("Invalid username or password.").isVisible()){
					throw new IllegalStateException("Not logged in but not where we thought.");
				}
				output.locator("#kc-registration").locator("a").click();
				output.waitForLoadState();

				output.locator("#firstName").fill(user.getFirstname());
				output.locator("#lastName").fill(user.getLastname());
				output.locator("#email").fill(user.getEmail());
				output.locator("#username").fill(user.getUsername());
				output.locator("#password").fill(user.getPassword());
				output.locator("#password-confirm").fill(user.getPassword());

				output.locator("#kc-form-buttons").locator(".pf-c-button").click();
				output.waitForLoadState();

			}

			if(!output.locator("#topOqmLogo").isVisible()){
				throw new IllegalStateException("Not logged in.");
			}
		} else {
			log.info("Was already logged in?");
		}

		return output;
	}
}
