package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
public class ItemsUiTest extends WebUiTest {

	/**
	 * This test verifies all data about an item that isn't about the stored items, including blocks associated with the item.
	 * @param itemType
	 */
	@ParameterizedTest
	@MethodSource("tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils#itemTypeArgs")
	public void testAddItemNoBlock(String itemType) {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);

		ItemsPage.assertNoItems(oqm);

		oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE).locator(ItemsPage.SEARCH_RESULTS_NONE_ADD_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		String expectedName = FAKER.appliance().equipment();
		String expectedDescription = FAKER.lorem().paragraph();

		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_NAME).fill(expectedName);
		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_DESCRIPTION).fill(expectedDescription);
		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_TYPE).selectOption(itemType);


		List<String> keywords = AttKeywordUiUtils.fillKeywords(oqm.locator(ItemsPage.ADD_EDIT_FORM), 5);
		Map<String, String> atts = AttKeywordUiUtils.fillAtts(oqm.locator(ItemsPage.ADD_EDIT_FORM), 5);

		oqm.locator(ItemsPage.ADD_EDIT_FORM_SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		log.info("first: {}", oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first());

		oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first()
			.locator(".viewButton")
			.click();

		MainAssertions.assertDoneProcessing(oqm);

		assertEquals(
			expectedName,
			oqm.locator(ItemsPage.VIEW_NAME).textContent()
		);
		assertEquals(
			expectedDescription,
			oqm.locator(ItemsPage.VIEW_DESCRIPTION).textContent()
		);

		Locator viewModal = oqm.locator(ItemsPage.VIEW_MODAL);

		assertTrue(viewModal.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_CONTAINER).isVisible());

		HistoryUiUtils.assertHistory(viewModal, List.of("CREATE"));

		AttKeywordUiUtils.assertKeywords(viewModal, keywords);
		AttKeywordUiUtils.assertAtts(viewModal, atts);



		//TODO:: types of item
		//TODO:: images, files
		//TODO:: categories

		//TODO:: more
	}

	/**
	 * This tests that storage blocks can be added to the item.
	 * @param itemType
	 */
	@ParameterizedTest
	@MethodSource("tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils#itemTypeArgs")
	public void testAddItemBlock(String itemType) {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);

		ObjectNode storageBlock = StorageBlockUiUtils.newStorageBlock(oqm);

		ItemsPage.assertNoItems(oqm);

		oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE).locator(ItemsPage.SEARCH_RESULTS_NONE_ADD_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		String expectedName = FAKER.appliance().equipment();
		String expectedDescription = FAKER.lorem().paragraph();

		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_NAME).fill(expectedName);
		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_DESCRIPTION).fill(expectedDescription);
		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_TYPE).selectOption(itemType);

		StorageSearchSelectUiUtils.select(
			storageBlock,
			oqm,
			ItemsPage.ADDEDIT_FORM_ADD_STORAGE_BUTTON
		);

		oqm.locator(ItemsPage.ADD_EDIT_FORM_SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		log.info("first: {}", oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first());

		oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first()
			.locator(".viewButton")
			.click();

		MainAssertions.assertDoneProcessing(oqm);

		assertEquals(
			expectedName,
			oqm.locator(ItemsPage.VIEW_NAME).textContent()
		);
		assertEquals(
			expectedDescription,
			oqm.locator(ItemsPage.VIEW_DESCRIPTION).textContent()
		);

		Locator viewModal = oqm.locator(ItemsPage.VIEW_MODAL);

		Locator nothingStoredContainer = viewModal.locator("#itemViewStoredNonePresentContainer");
		assertTrue(nothingStoredContainer.isVisible());

		List<Locator> storageBlocksInItemLinks = nothingStoredContainer.locator("#itemViewStoredNonePresentBlocksList").locator("a").all();

		assertEquals(
			storageBlock.get("label").asText(),
			storageBlocksInItemLinks.getFirst().locator("span").textContent().strip()
		);
	}

	//TODO:: edit
	//TODO:: id copy button
	//TODO:: transactions
}
