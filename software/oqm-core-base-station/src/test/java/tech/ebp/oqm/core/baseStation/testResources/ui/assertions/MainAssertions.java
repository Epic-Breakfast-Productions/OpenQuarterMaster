package tech.ebp.oqm.core.baseStation.testResources.ui.assertions;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainAssertions {

	public static void assertDoneProcessing(Page page) throws InterruptedException {
		Thread.sleep(500);
		page.waitForLoadState();
		log.info("Waiting for page processes to finish up.");
		page.waitForFunction("()=>Main.noProcessesRunning();");
		log.info("DONE waiting for page processes to finish up.");
	}

	public static void assertMainPageAlert(Page page, String type, String content){
		//TODO
	}
}
