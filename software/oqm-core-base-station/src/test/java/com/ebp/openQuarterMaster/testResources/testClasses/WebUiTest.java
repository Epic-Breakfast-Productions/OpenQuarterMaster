package com.ebp.openQuarterMaster.testResources.testClasses;

import com.ebp.openQuarterMaster.testResources.OurTestDescription;
import com.ebp.openQuarterMaster.testResources.lifecycleManagers.SeleniumGridServerManager;
import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.testResources.ui.WebDriverWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;

import java.util.Optional;

/**
 * TODO:: move to use playwright
 *  - https://playwright.dev/java/docs/videos
 *
 *
 *
 */
@Slf4j
@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {

//	@Getter(AccessLevel.PROTECTED)
//	private WebDriverWrapper webDriverWrapper = new WebDriverWrapper();


//	@BeforeEach
//	public void beforeEachUI(TestInfo testInfo){
//		log.info("Before test (UI) " + testInfo.getTestMethod().get().getName());
//
//		if(SeleniumGridServerManager.RECORD) {
//			TestResourceLifecycleManager.BROWSER_CONTAINER.beforeTest(
//				new OurTestDescription(testInfo)
//			);
//		}
//	}
//
//	@AfterEach
//	public void afterEachUI(
//		TestInfo testInfo
//	) {
//		log.info("Running after method for test (UI) {}", testInfo.getDisplayName());
//
//		if(SeleniumGridServerManager.RECORD) {
//			TestResourceLifecycleManager.BROWSER_CONTAINER.triggerRecord(
//				new OurTestDescription(testInfo),
//				//TODO:: actually pass something real https://stackoverflow.com/questions/71354431/junit5-get-results-from-test-in-aftereach
//				Optional.empty()
//			);
//		}
////		findAndCleanupWebDriverWrapper();
//		this.getWebDriverWrapper().cleanup();
//
//		log.info("Completed after step (UI).");
//	}

}
