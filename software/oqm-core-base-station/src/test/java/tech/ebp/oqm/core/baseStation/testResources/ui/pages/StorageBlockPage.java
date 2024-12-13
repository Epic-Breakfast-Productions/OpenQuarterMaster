package tech.ebp.oqm.core.baseStation.testResources.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageBlockPage {

	public static final String STORAGE_PAGE = "/storage";

	public static final String SEARCH_RESULTS_TABLE = "#mainPageSearchResults";
	public static final String SEARCH_RESULTS_NONE_ADD_BUTTON = ".noResultsAddButton";

	public static void assertNoStorageBlocks(Page page) {
		log.info("Asserting no storage blocks on storage block page.");

		page.locator(SEARCH_RESULTS_TABLE).locator(SEARCH_RESULTS_NONE_ADD_BUTTON);
	}
}
