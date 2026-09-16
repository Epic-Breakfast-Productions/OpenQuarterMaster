package tech.ebp.oqm.core.baseStation.interfaces.ui.pages.transactions.add;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MessageAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.StorageBlockUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.transaction.AddTransactionUtils;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
public class AddAmtListTransactionUiTest extends WebUiTest {
	
	@Test
	public void newAmount() {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);
		
		List<ObjectNode> storageBlocks = List.of(
			StorageBlockUiUtils.newStorageBlock(oqm),
			StorageBlockUiUtils.newStorageBlock(oqm)
		);
		
		ObjectNode item = ItemsUiUtils.newItem(
			oqm,
			ObjectUtils.OBJECT_MAPPER.createObjectNode().put(ItemsUiUtils.OBJ_KEY_TYPE, "AMOUNT_LIST"),
			storageBlocks
		);
		
		ItemsUiUtils.viewItem(oqm, item);
		
		oqm.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_ADD_STORED_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		
		oqm.locator(AddTransactionUtils.TYPE_INPUT).selectOption("ADD_AMOUNT");
		MainAssertions.assertDoneProcessing(oqm);
		
		Locator attTransactionModal = oqm.locator(AddTransactionUtils.MODAL);
		Locator addStoredInputs = attTransactionModal.locator(AddTransactionUtils.INPUTS_CONTAINER);
		
		assertTrue(
			addStoredInputs
				.locator(AddTransactionUtils.AMOUNT_INPUTS_CONTAINER)
				.isVisible()
		);
		assertFalse(
			addStoredInputs
				.locator(AddTransactionUtils.COMMON_INPUTS_CONTAINER)
				.isVisible()
		);
		assertFalse(
			addStoredInputs
				.locator(AddTransactionUtils.UNIQUE_INPUTS_CONTAINER)
				.isVisible()
		);
		
		oqm.locator(AddTransactionUtils.TO_BLOCK_SELECT).selectOption(storageBlocks.getFirst().get("id").asText());
		
		addStoredInputs.locator(AddTransactionUtils.AMOUNT_VALUE_INPUT).fill("5");
		
		oqm.locator(AddTransactionUtils.SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		
		MessageAssertions.assertMessage(oqm, MessageAssertions.SUCCESS_MESSAGE, "Success!", "Transaction Successful!");
		
		//TODO:: assert added stored matches
	}
	
	@Test
	public void newWhole() {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);
		
		List<ObjectNode> storageBlocks = List.of(
			StorageBlockUiUtils.newStorageBlock(oqm),
			StorageBlockUiUtils.newStorageBlock(oqm)
		);
		
		ObjectNode item = ItemsUiUtils.newItem(
			oqm,
			ObjectUtils.OBJECT_MAPPER.createObjectNode().put(ItemsUiUtils.OBJ_KEY_TYPE, "AMOUNT_LIST"),
			storageBlocks
		);
		
		ItemsUiUtils.viewItem(oqm, item);
		
		oqm.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_ADD_STORED_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		
		oqm.locator(AddTransactionUtils.TYPE_INPUT).selectOption("ADD_WHOLE");
		MainAssertions.assertDoneProcessing(oqm);
		
		Locator attTransactionModal = oqm.locator(AddTransactionUtils.MODAL);
		Locator addStoredInputs = attTransactionModal.locator(AddTransactionUtils.INPUTS_CONTAINER);
		
		assertTrue(
			addStoredInputs
				.locator(AddTransactionUtils.AMOUNT_INPUTS_CONTAINER)
				.isVisible()
		);
		assertTrue(
			addStoredInputs
				.locator(AddTransactionUtils.COMMON_INPUTS_CONTAINER)
				.isVisible()
		);
		assertFalse(
			addStoredInputs
				.locator(AddTransactionUtils.UNIQUE_INPUTS_CONTAINER)
				.isVisible()
		);
		
		oqm.locator(AddTransactionUtils.TO_BLOCK_SELECT).selectOption(storageBlocks.getFirst().get("id").asText());
		
		addStoredInputs.locator(AddTransactionUtils.AMOUNT_VALUE_INPUT).fill("5");
		
		oqm.locator(AddTransactionUtils.SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		
		MessageAssertions.assertMessage(oqm, MessageAssertions.SUCCESS_MESSAGE, "Success!", "Transaction Successful!");
		
		//TODO:: assert added stored matches
	}
	
	
	//TODO:: existing amount list, amount
	//TODO:: existing amount list, amount to existing
	//TODO:: existing amount list, whole
	
}
