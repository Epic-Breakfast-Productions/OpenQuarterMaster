package tech.ebp.oqm.core.baseStation.interfaces.ui.pages.transactions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.*;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.transaction.AddTransactionUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
public class AddTransactionUiTest extends WebUiTest {

	/**
	 *
	 */
	@Test
	public void testAddAmountToNewBulk() {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);

		List<ObjectNode> storageBlocks = List.of(
			StorageBlockUiUtils.newStorageBlock(oqm),
			StorageBlockUiUtils.newStorageBlock(oqm)
		);

		ObjectNode item = ItemsUiUtils.newItem(oqm, storageBlocks);

		ItemsUiUtils.viewItem(oqm, item);

		oqm.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_ADD_STORED_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		Locator attTransactionModal = oqm.locator(AddTransactionUtils.MODAL);
		assertTrue(attTransactionModal.isVisible());

		assertFalse(attTransactionModal.locator(AddTransactionUtils.ITEM_INPUT_CONTAINER).isVisible());
		assertTrue(attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT_CONTAINER).isVisible());

		assertFalse(attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT).isEditable());
		assertEquals("ADD_AMOUNT", attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT).inputValue());

		assertTrue(attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_CONTAINER).isVisible());
		assertTrue(attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_RADIO).isChecked());

		assertFalse(attTransactionModal.locator(AddTransactionUtils.TO_STORED_CONTAINER).isVisible());

		Locator addStoredInputs = attTransactionModal.locator(AddTransactionUtils.INPUTS_CONTAINER);
		assertTrue(
			addStoredInputs
				.locator(AddTransactionUtils.AMOUNT_INPUTS_CONTAINER)
				.isVisible()
		);

		addStoredInputs.locator(AddTransactionUtils.AMOUNT_VALUE_INPUT).fill("5");

		attTransactionModal.locator(AddTransactionUtils.SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);

		ItemsUiUtils.viewItem(oqm, item);

		assertEquals(
			"5units",
			oqm.locator(ItemsPage.VIEW_TOTAL).textContent().strip()
		);

		//TODO:: fix issue making this fail on rendering side
		//TODO:: assert stored accord
	}

}
