package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class HistoryUiUtils {
	public static final String HIST_VIEW_ACCORD_HEADER = ".historyViewAccordionHeader";
	public static final String HIST_VIEW_TABLE = ".historyViewResults";


	public static void assertHistory(
		Locator container,
		List<String> expectedEvents
	){
		container.locator(HIST_VIEW_ACCORD_HEADER).click();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		Locator historyRowsLocator = container.locator(HIST_VIEW_TABLE).locator("tbody")
			.locator("tr");
		assertEquals(
			expectedEvents.size(),
			historyRowsLocator.count()
		);

		List<Locator> historyRows = historyRowsLocator.all();

		for (int i = 0; i < historyRows.size(); i++) {
			Locator rowLocator = historyRows.get(i);
			String curExpectedEventType = expectedEvents.get(i);

			assertEquals(
				curExpectedEventType,
				rowLocator.locator(".event-type").textContent().strip()
			);
		}



	}
}
