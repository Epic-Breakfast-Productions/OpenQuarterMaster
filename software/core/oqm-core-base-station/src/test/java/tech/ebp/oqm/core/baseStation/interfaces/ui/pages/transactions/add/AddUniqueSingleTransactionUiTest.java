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
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.AttKeywordUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.StorageBlockUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.transaction.AddTransactionUtils;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
public class AddUniqueSingleTransactionUiTest extends WebUiTest {
	
	@Test
	public void testAdd() {
		Page oqm = this.getLoggedInPage(this.getTestUserService().getTestUser(), ItemsPage.ITEMS_PAGE);
		
		List<ObjectNode> storageBlocks = List.of(
			StorageBlockUiUtils.newStorageBlock(oqm),
			StorageBlockUiUtils.newStorageBlock(oqm)
		);
		
		ObjectNode item = ItemsUiUtils.newItem(
			oqm,
			ObjectUtils.OBJECT_MAPPER.createObjectNode().put(ItemsUiUtils.OBJ_KEY_TYPE, "UNIQUE_SINGLE"),
			storageBlocks
		);
		
		ItemsUiUtils.viewItem(oqm, item);
		
		oqm.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_ADD_STORED_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		
		Locator attTransactionModal = oqm.locator(AddTransactionUtils.MODAL);
		assertTrue(attTransactionModal.isVisible());
		
		assertFalse(attTransactionModal.locator(AddTransactionUtils.ITEM_INPUT_CONTAINER).isVisible());
		assertTrue(attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT_CONTAINER).isVisible());
		
		assertFalse(attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT).isEditable());
		assertEquals("ADD_WHOLE", attTransactionModal.locator(AddTransactionUtils.TYPE_INPUT).inputValue());
		
		assertTrue(attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_CONTAINER).isVisible());
		assertTrue(attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_RADIO).isChecked());
		assertFalse(attTransactionModal.locator(AddTransactionUtils.TO_STORED_CONTAINER).isVisible());
		
		Locator addStoredInputs = attTransactionModal.locator(AddTransactionUtils.INPUTS_CONTAINER);
		assertFalse(
			addStoredInputs
				.locator(AddTransactionUtils.AMOUNT_INPUTS_CONTAINER)
				.isVisible()
		);
		assertTrue(
			addStoredInputs
				.locator(AddTransactionUtils.COMMON_INPUTS_CONTAINER)
				.isVisible()
		);
//		assertTrue(//no inputs
//			addStoredInputs
//				.locator(AddTransactionUtils.UNIQUE_INPUTS_CONTAINER)
//				.isVisible()
//		);
	
		String condition = "100";
		String conditionNotes = FAKER.lorem().paragraph();
		
		attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_SELECT).selectOption(storageBlocks.getFirst().get("id").asText());
		
		addStoredInputs.locator(AddTransactionUtils.COMMON_CONDITION_INPUT).fill(condition);
		addStoredInputs.locator(AddTransactionUtils.COMMON_CONDITION_NOTES_INPUT).fill(conditionNotes);
		List<String> keywords = AttKeywordUiUtils.fillKeywords(addStoredInputs, 5);
		Map<String, String> atts = AttKeywordUiUtils.fillAtts(addStoredInputs, 5);
		
		//TODO:: expires
		//TODO:: image, file (need to implement)
		
		attTransactionModal.locator(AddTransactionUtils.SUBMIT_BUTTON).click();
		MainAssertions.assertDoneProcessing(oqm);
		MessageAssertions.assertMessage(oqm, MessageAssertions.SUCCESS_MESSAGE, "Success!", "Transaction Successful!");
		
		ItemsUiUtils.viewItem(oqm, item);
		
		assertEquals(
			"1units",
			oqm.locator(ItemsPage.VIEW_TOTAL).textContent().strip()
		);
		
		Locator storedInLabel = oqm.locator(".uniqueItemStoredInLabel");
		assertTrue(storedInLabel.isVisible());
		assertEquals(
			storageBlocks.getFirst().get("label").asText(),
			storedInLabel.locator("span").textContent().strip()
		);
		Locator alsoStoredInLabel = oqm.locator(".uniqueItemStoredAlsoInLabel");
		assertTrue(alsoStoredInLabel.isVisible());
		assertEquals(
			storageBlocks.getLast().get("label").asText(),
			alsoStoredInLabel.locator("span").textContent().strip()
		);
		
		Locator storedViewContainer = oqm.locator("#itemViewStoredSingleContainer");
		assertTrue(storedViewContainer.isVisible());
		
		assertTrue(storedViewContainer.locator(".storedCondition").isVisible());
		assertEquals(
			condition + "%",
			storedViewContainer.locator(".storedCondition").textContent().strip()
		);
		assertTrue(storedViewContainer.locator(".storedConditionNotes").isVisible());
		assertEquals(
			conditionNotes,
			storedViewContainer.locator(".storedConditionNotes").textContent().strip()
		);
		
		AttKeywordUiUtils.assertKeywords(storedViewContainer.locator(".keywordsViewContainer").locator(".."), keywords);
		AttKeywordUiUtils.assertAtts(storedViewContainer.locator(".attsViewContainer").locator(".."), atts);
		
	}
	
}
