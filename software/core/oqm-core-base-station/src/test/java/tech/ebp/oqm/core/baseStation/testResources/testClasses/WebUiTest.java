package tech.ebp.oqm.core.baseStation.testResources.testClasses;

import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.RecordVideoSize;
import com.microsoft.playwright.options.ScreenSize;
import com.microsoft.playwright.options.ViewportSize;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUser;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUserService;
import tech.ebp.oqm.core.baseStation.testResources.ui.PlaywrightSetup;
import com.microsoft.playwright.*;
import io.quarkus.test.common.http.TestHTTPResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.NavUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.StorageBlockUiUtils;

import javax.swing.text.View;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {

	private static Path getCurTestDir(TestInfo testInfo) {
		log.debug("Display name: {}", testInfo.getDisplayName());

		Path output = PlaywrightSetup.RECORDINGS_DIR.resolve(testInfo.getTestClass().get().getName());

		if (testInfo.getDisplayName().startsWith("[")) {
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
	private BrowserContext context;

	@Getter
	private Path curTestUiResultDir;

	Map<Integer, OutputStream> consoleOutputs = new HashMap<>();

	@BeforeEach
	public void beforeEachUi(TestInfo testInfo) {
		StorageBlockUiUtils.resetBlockNames();
		this.curTestUiResultDir = getCurTestDir(testInfo);
		ScreenSize screenSize = new ScreenSize(1920, 1080);

		Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions()
			.setRecordVideoDir(this.curTestUiResultDir)
			.setRecordHarPath(this.curTestUiResultDir.resolve("har.json"))
			.setScreenSize(screenSize)
			.setRecordVideoSize(new RecordVideoSize(screenSize.width, screenSize.height))
			.setViewportSize(new ViewportSize(screenSize.width, screenSize.height));

		this.context = PlaywrightSetup.getInstance().getBrowser().newContext(newContextOptions);
	}

	@AfterEach
	public void afterEachUi(TestInfo testInfo) throws InterruptedException, IOException {

		for (int i = 0; i < this.getContext().pages().size(); i++) {
			Page curPage = this.getContext().pages().get(i);
			Path curPageFinalScreenshot = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-final.png");
			Path curPageHtmlFile = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-final-code.html");
			Path curPageInfoFile = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-final-info.txt");

			curPage.screenshot(getScreenshotOptions().setPath(curPageFinalScreenshot));
			try (OutputStream outputStream = new FileOutputStream(curPageHtmlFile.toFile())) {
				outputStream.write(curPage.content().getBytes());
			}
			try (
				OutputStream outputStream = new FileOutputStream(curPageInfoFile.toFile());
				PrintWriter pw = new PrintWriter(outputStream);
			) {
				pw.println("Test info:");
				pw.println();
				pw.println("Final url: " + curPage.url());
				pw.println();
			}
		}
		Thread.sleep(250);
		for (OutputStream outputStream : this.consoleOutputs.values()) {
			outputStream.close();
		}
		this.context.close();
	}

	protected Page getPage() {
		Page output = this.getContext().newPage();

		int i = this.getContext().pages().indexOf(output);
		Path curPageConsoleFile = this.curTestUiResultDir.resolve("page-" + (i + 1) + "-console.log");
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(curPageConsoleFile.toFile());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.consoleOutputs.put(i, outputStream);

		OutputStream finalOutputStream = outputStream;
		output.onPageError(consoleError -> {
			try {
				finalOutputStream.write(
					String.format(
						"\n\nERROR on page: %s\n\n",
						new String(consoleError.getBytes(StandardCharsets.UTF_8))
					).getBytes()
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		output.onConsoleMessage(consoleMessage -> {
			StringBuilder message = new StringBuilder();
			if(!consoleMessage.args().isEmpty()){
				boolean first = true;
				for (JSHandle curHandle : consoleMessage.args()){
					if(first){
						first = false;
						message.append("\t");
					}
					try {
						message.append(curHandle.jsonValue().toString().strip()).append("\n");
					} catch(PlaywrightException e){
						log.warn("Failed to get json value for handle: {}", curHandle, e);
						break;
					}
				}
			}

			try {
				finalOutputStream.write(
					String.format(
						"[%s][%s] %s\n",
						consoleMessage.location(),
						consoleMessage.type(),
						message.toString()
					).getBytes()
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		return output;
	}

	protected Page getLoggedInPage(TestUser user, String page) {
		Page output = this.getPage();

		NavUtils.navigateToEndpoint(output, page);

		if (output.title().contains("Sign in to Open QuarterMaster")) {
			log.info("Need to log in user.");

			Locator locator = output.locator("#password");
			locator.fill(user.getPassword());
			locator = output.locator("#username");
			locator.fill(user.getUsername());

			output.locator("#kc-login").click();
			output.waitForLoadState();

			if (!output.locator("#topOqmLogo").isVisible()) {
				if (!output.getByText("Invalid username or password.").isVisible()) {
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

				output.locator("#kc-form-buttons").locator("input").click();
				output.waitForLoadState();

			}

			if (!output.locator("#topOqmLogo").isVisible()) {
				throw new IllegalStateException("Not logged in.");
			}
			user.setJwt(output);
			log.info("Logged in user: " + user);
		} else {
			log.info("Was already logged in?");
		}

		return output;
	}
}
