package tech.ebp.oqm.core.baseStation.testResources.ui.assertions;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.NavUtils;

@Slf4j
public class MainAssertions {

	public static void assertDoneProcessing(Page page) {
		log.info("Waiting for page to load and things to finish processing.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		page.waitForLoadState();

		if(page.url().startsWith(NavUtils.getIndex().toString())){
			log.info("Waiting for page processes to finish up.");
			page.waitForFunction("()=>Main.noProcessesRunning();");
			log.info("DONE waiting for page processes to finish up.");

			//TODO:: check for console errors?
//			page.onConsoleMessage();
		} else {
			log.info("NOT on OQM Base Station page: {}", page.url());
		}
		log.info("DONE Waiting for page to load and things to finish processing.");
	}
}
