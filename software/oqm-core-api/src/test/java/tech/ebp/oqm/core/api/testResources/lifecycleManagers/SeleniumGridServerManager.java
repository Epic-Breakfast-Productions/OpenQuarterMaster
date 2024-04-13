package tech.ebp.oqm.core.api.testResources.lifecycleManagers;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.shaded.org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

@Slf4j
public class SeleniumGridServerManager implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
	
	public static final boolean RECORD = true;
	
	private BrowserWebDriverContainer<?> browserWebDriverContainer = null;
	private Optional<String> containerNetworkId;
	
	private boolean uiTest = false;
	
	@Getter
	private WebDriver driver = null;
	
	@Override
	public Map<String, String> start() {
		if (!uiTest) {
			log.info("Test not calling for ui.");
			return Map.of();
		}
		if (this.browserWebDriverContainer == null || !this.browserWebDriverContainer.isRunning()) {
			log.info("Starting Selenium WebGrid server.");
			StopWatch sw = StopWatch.createStarted();
			
			this.browserWebDriverContainer = new BrowserWebDriverContainer<>()
												 .withCapabilities(new FirefoxOptions())
												 .withReuse(false)
												 .withAccessToHost(true)
//												 .withNetworkAliases("host.docker.internal:localhost")
			;
			
			if (RECORD) {
				File recordingDir = new File(
					"build/seleniumRecordings/"
					+ new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date())
				);
				recordingDir.mkdirs();
				this.browserWebDriverContainer = this.browserWebDriverContainer.withRecordingMode(
					RECORD_ALL,
					recordingDir,
					VncRecordingContainer.VncRecordingFormat.MP4
				);
				
			}
			this.browserWebDriverContainer.start();
			
			sw.stop();
			
			log.info(
				"Started selenium testcontainer in {}: {}; {}",
				sw,
				this.browserWebDriverContainer.getDockerImageName(),
				this.browserWebDriverContainer.isCreated() && this.browserWebDriverContainer.isRunning()
			);
			log.info("Getting web driver");
			this.driver = this.browserWebDriverContainer.getWebDriver();
			log.info("Got web driver.");
		} else {
			log.info("Selenium grid server already running.");
		}
		return Map.of(
			"runningInfo.hostname",
			Utils.HOST_TESTCONTAINERS_INTERNAL
			
//			"quarkus.keycloak.devservices.enabled",
//			"true",
//
//			"quarkus.oidc.auth-server-url",
//			"http://"+Utils.HOST_TESTCONTAINERS_INTERNAL+":8089/realms/oqm"
		);
	}
	
	@Override
	public void stop() {
		if (this.browserWebDriverContainer == null) {
			log.info("Web browser container never started.");
			return;
		}
		
		log.info("Stopping web driver container.");
		
		if (this.driver != null) {
			this.driver.close();
		}
		if (this.browserWebDriverContainer != null) {
			this.browserWebDriverContainer.close();
			this.browserWebDriverContainer.stop();
		}
		
		this.driver = null;
		this.browserWebDriverContainer = null;
		
		log.info("Stopped web driver server.");
	}
	
	public void triggerRecord(TestDescription description, Optional<Throwable> throwable) {
		if (this.browserWebDriverContainer != null) {
			log.info("Triggering browser container to save recording of test.");
			this.browserWebDriverContainer.afterTest(description, throwable);
		}
	}
	
	public void beforeTest(TestDescription description) {
		if (this.browserWebDriverContainer != null) {
			this.browserWebDriverContainer.beforeTest(description);
			
		}
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		QuarkusTestResourceLifecycleManager.super.init(initArgs);
		this.uiTest = Boolean.parseBoolean(initArgs.getOrDefault(TestResourceLifecycleManager.UI_TEST_ARG, Boolean.toString(this.uiTest)));
	}
	
	@Override
	public void setIntegrationTestContext(DevServicesContext context) {
		containerNetworkId = context.containerNetworkId();
	}
	
	//	public WebDriver getDriver() {
	//		return this.browserWebDriverContainer.getWebDriver();
	//	}
}