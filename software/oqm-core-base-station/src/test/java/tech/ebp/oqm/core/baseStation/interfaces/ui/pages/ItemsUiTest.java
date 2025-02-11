package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.AttKeywordUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.HistoryUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.StorageBlockUiUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
public class ItemsUiTest extends WebUiTest {

	@Test
	public void testAddItem() {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);

		ObjectNode storageBlock = StorageBlockUiUtils.newStorageBlock(oqm);

		ItemsPage.assertNoItems(oqm);


		oqm.locator(ItemsPage.SEARCH_RESULTS_TABLE).locator(ItemsPage.SEARCH_RESULTS_NONE_ADD_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		String expectedName = FAKER.appliance().equipment();
		String expectedDescription = FAKER.lorem().paragraph();

		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_NAME).fill(expectedName);
		oqm.locator(ItemsPage.ADDEDIT_FORM_INPUT_DESCRIPTION).fill(expectedDescription);

		List<String> keywords = AttKeywordUiUtils.fillKeywords(oqm.locator(ItemsPage.ADD_EDIT_FORM), 5);
		Map<String, String> atts = AttKeywordUiUtils.fillAtts(oqm.locator(ItemsPage.ADD_EDIT_FORM), 5);

		//TODO:: add block

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

		HistoryUiUtils.assertHistory(viewModal, List.of("CREATE"));

		AttKeywordUiUtils.assertKeywords(viewModal, keywords);
		AttKeywordUiUtils.assertAtts(viewModal, atts);

		//TODO:: types of item
		//TODO:: images, files, att/keywords
		//TODO:: history view
		//TODO:: categories

		//TODO:: more
	}

	//TODO:: edit
	//TODO:: id copy button
	//TODO:: transactions
}
