package tech.ebp.oqm.core.baseStation.testResources.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageBlockPage {

	public static final String STORAGE_PAGE = "/storage";

	public static final String SEARCH_RESULTS_TABLE = "#mainPageSearchResults";
	public static final String SEARCH_RESULTS_NONE_ADD_BUTTON = ".noResultsAddButton";
	// add/edit form
	public static final String ADDEDIT_FORM = "#addEditStorageForm";
	public static final String ADDEDIT_FORM_SUBMIT_BUTTON = "#addEditFormSubmitButton";
	public static final String ADDEDIT_FORM_LABEL_INPUT = "#addEditLabelInput";
	public static final String ADDEDIT_FORM_NICKNAME_INPUT = "#addEditNicknameInput";
	public static final String ADDEDIT_FORM_DESCRIPTION_INPUT = "#addEditDescriptionInput";
	public static final String ADDEDIT_FORM_LOCATION_INPUT = "#addEditLocationInput";

	public static final String VIEW_MODAL_LABEL = "#storageBlockViewModalLabel";
	public static final String VIEW_ID = "#storageBlockViewId";
	public static final String VIEW_DESCRIPTION = "#storageBlockViewDescription";
	public static final String VIEW_LOCATION = "#storageBlockViewLocation";

	public static void assertNoStorageBlocks(Page page) {
		log.info("Asserting no storage blocks on storage block page.");

		page.locator(SEARCH_RESULTS_TABLE).locator(SEARCH_RESULTS_NONE_ADD_BUTTON);
	}
}
