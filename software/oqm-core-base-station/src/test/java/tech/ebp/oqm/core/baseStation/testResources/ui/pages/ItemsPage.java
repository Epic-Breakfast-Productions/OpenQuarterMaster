package tech.ebp.oqm.core.baseStation.testResources.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemsPage {

	public static final String ITEMS_PAGE = "/items";

	public static final String MAIN_ADD_BUTTON = "#mainAddButton";
	public static final String SEARCH_RESULTS_TABLE = "#mainPageSearchResults";
	public static final String SEARCH_RESULTS_NONE_ADD_BUTTON = ".noResultsAddButton";
	// add/edit form
	public static final String ADD_EDIT_FORM = "#addEditItemForm";
	public static final String ADD_EDIT_FORM_SUBMIT_BUTTON = "#addEditItemFormSubmitButton";
	public static final String ADDEDIT_FORM_INPUT_NAME = "#addEditItemNameInput";
	public static final String ADDEDIT_FORM_INPUT_DESCRIPTION = "#addEditItemDescriptionInput";
	public static final String ADDEDIT_FORM_INPUT_TYPE = "#addEditItemStorageTypeInput";
	public static final String ADDEDIT_FORM_ADD_STORAGE_BUTTON = "#addEditItemAssociatedStorageAddButton";
	// view modal
	public static final String VIEW_MODAL = "#itemViewModal";
	public static final String VIEW_NAME = "#itemViewModalLabel";
	public static final String VIEW_DESCRIPTION = "#itemViewDescription";
	public static final String VIEW_NONE_PRESENT_NO_STORAGE_CONTAINER = "#itemViewStoredNonePresentNoStorageContainer";


	public static void assertNoItems(Page page) {
		log.info("Asserting no items on items page.");

		page.locator(SEARCH_RESULTS_TABLE).locator(SEARCH_RESULTS_NONE_ADD_BUTTON);
	}
}
