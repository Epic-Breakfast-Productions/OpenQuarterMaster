package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;

@Slf4j
@QuarkusTest
public class StorageBlockUiTest extends WebUiTest {

	@Test
	public void testAddStorageBlock() throws InterruptedException {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), StorageBlockPage.STORAGE_PAGE);

		StorageBlockPage.assertNoStorageBlocks(oqm);

		oqm.locator(StorageBlockPage.SEARCH_RESULTS_TABLE).locator(StorageBlockPage.SEARCH_RESULTS_NONE_ADD_BUTTON).click();

		MainAssertions.assertDoneProcessing(oqm);
		//TODO:: more
	}
}
