package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.List;

@Slf4j
public class StorageSearchSelectUiUtils {
	private static final String STORAGE_SEARCH_SELECT_RESULT = "#storageSearchSelectResults";

	public static void select(ObjectNode block, Page page, String searchButton){
		page.locator(searchButton).click();
		MainAssertions.assertDoneProcessing(page);
		String toFind = block.get("label").asText();

		Locator searchResults = page.locator(STORAGE_SEARCH_SELECT_RESULT);

		List<Locator> resultRows = searchResults.locator("tbody").locator("tr").all();

		for(Locator resultRow : resultRows){
			String curLabel = resultRow.locator(".blockSearchResultLabel").textContent().trim();

			if(curLabel.equals(toFind)){
				resultRow.locator(".blockSearchSelectButton").click();
				MainAssertions.assertDoneProcessing(page);
				return;
			}
		}
		Assertions.fail("Could not find expected search result.");
	}
}
