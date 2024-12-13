package tech.ebp.oqm.core.baseStation.testResources.ui.assertions;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainAssertions {

	public static void assertDoneProcessing(Page page) throws InterruptedException {
		log.info("Waiting for page processes to finish up.");
		Thread.sleep(250);
		page.waitForFunction("()=>Main.noProcessesRunning();");
		log.info("DONE waiting for page processes to finish up.");
	}
}
