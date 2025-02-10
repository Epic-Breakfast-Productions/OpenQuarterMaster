package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.AttKeywordUiUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
public class StorageBlockUiTest extends WebUiTest {

	@Test
	public void testAddStorageBlock() throws InterruptedException {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), StorageBlockPage.STORAGE_PAGE);

		StorageBlockPage.assertNoStorageBlocks(oqm);

		oqm.locator(StorageBlockPage.SEARCH_RESULTS_TABLE).locator(StorageBlockPage.SEARCH_RESULTS_NONE_ADD_BUTTON).click();

		MainAssertions.assertDoneProcessing(oqm);

		String expectedLabel = FAKER.location().building();
		String expectedNickname = FAKER.name().title();
		String expectedDescription = FAKER.lorem().paragraph();
		String expectedLocation = FAKER.location().nature();

		oqm.locator(StorageBlockPage.ADDEDIT_FORM_LABEL_INPUT).fill(expectedLabel);
		oqm.locator(StorageBlockPage.ADDEDIT_FORM_NICKNAME_INPUT).fill(expectedNickname);
		oqm.locator(StorageBlockPage.ADDEDIT_FORM_DESCRIPTION_INPUT).fill(expectedDescription);
		oqm.locator(StorageBlockPage.ADDEDIT_FORM_LOCATION_INPUT).fill(expectedLocation);

		List<String> keywords = AttKeywordUiUtils.fillKeywords(oqm.locator(StorageBlockPage.ADDEDIT_FORM), 5);
		Map<String, String> atts = AttKeywordUiUtils.fillAtts(oqm.locator(StorageBlockPage.ADDEDIT_FORM), 5);

		oqm.locator(StorageBlockPage.ADDEDIT_FORM_SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);


		log.info("first: {}", oqm.locator(StorageBlockPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first());

		oqm.locator(StorageBlockPage.SEARCH_RESULTS_TABLE)
			.locator("tbody")
			.first()
			.locator(".viewButton")
			.click();

		MainAssertions.assertDoneProcessing(oqm);

		assertEquals(
			expectedLabel + " / " + expectedNickname,
			oqm.locator(StorageBlockPage.VIEW_MODAL_LABEL).textContent()
		);
		assertEquals(
			expectedDescription,
			oqm.locator(StorageBlockPage.VIEW_DESCRIPTION).textContent()
		);
		assertEquals(
			expectedLocation,
			oqm.locator(StorageBlockPage.VIEW_LOCATION).textContent()
		);

		oqm.locator(StorageBlockPage.VIEW_HISTORY_EXPAND).click();
		Thread.sleep(250);

		Locator historyRows = oqm.locator(StorageBlockPage.VIEW_HISTORY_TABLE).locator("tbody")
			.locator("tr");
		assertEquals(
			1,
			historyRows.count()
		);

		assertEquals(
			"CREATE",
			historyRows.first().locator(".event-type").textContent().strip()
		);

		AttKeywordUiUtils.assertKeywords(oqm.locator(StorageBlockPage.VIEW_MODAL), keywords);
		AttKeywordUiUtils.assertAtts(oqm.locator(StorageBlockPage.VIEW_MODAL), atts);


		//TODO:: images, files

		//TODO:: categories

		//TODO:: more
	}

	//TODO:: edit
	//TODO:: parent blocks
	//TODO:: can see items in view list
	//TODO:: id copy button
	//TODO:: printouts
	//TODO:: bulk adding
	//TODO:: test empty field containers not shown
}
