package tech.ebp.oqm.core.baseStation.testResources.ui.utilities.transaction;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.NavUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class AddTransactionUtils {
	
	public static final String MODAL = "#itemStoredTransactionAddModal";
	public static final String ITEM_INPUT_CONTAINER = "#itemStoredTransactionAddFormItemInputContainer";
	public static final String TYPE_INPUT_CONTAINER = "#itemStoredTransactionAddFormTypeInputContainer";
	public static final String TYPE_INPUT = "#itemStoredTransactionAddFormTypeInput";
	public static final String TO_BLOCK_CONTAINER = "#itemStoredTransactionAddFormToBlockInputContainer";
	public static final String TO_BLOCK_RADIO = "#itemStoredTransactionAddFormToBlockRadio";
	public static final String TO_BLOCK_SELECT = "#itemStoredTransactionAddFormToBlockInput";
	public static final String TO_STORED_CONTAINER = "#itemStoredTransactionAddFormToStoredInputContainer";
	public static final String INPUTS_CONTAINER = "#itemStoredTransactionAddFormInputsContainer";
	public static final String AMOUNT_INPUTS_CONTAINER = ".amountStoredFormElements";
	public static final String AMOUNT_VALUE_INPUT = ".amountStoredValueInput";
	public static final String UNIQUE_INPUTS_CONTAINER = ".uniqueStoredFormInputs";
	public static final String COMMON_INPUTS_CONTAINER = ".commonStoredFormElements";
	public static final String COMMON_BARCODE_INPUT = ".storedBarcodeInput";
	public static final String COMMON_CONDITION_INPUT = ".storedConditionPercentageInput";
	public static final String COMMON_CONDITION_NOTES_INPUT = ".storedConditionNotesInput";
	public static final String SUBMIT_BUTTON = "#itemStoredTransactionAddFormSubmitButton";
	
	
	public static void doAddAmountTransaction(Page page, ObjectNode item, ObjectNode toBlock, int amount) {
		NavUtils.performOnOtherPage(
			page,
			ItemsUiUtils.getViewItemEndpoint(item),
			page1->{
				
				Locator nonePresentStoredAddButton = page1.locator(ItemsPage.VIEW_NONE_PRESENT_NO_STORAGE_ADD_STORED_BUTTON);
				
				if(nonePresentStoredAddButton.isVisible()){
					nonePresentStoredAddButton.click();
				} else {
					//TODO:: open accord, open transaction from there
				}
				
				MainAssertions.assertDoneProcessing(page1);
				
				Locator attTransactionModal = page1.locator(AddTransactionUtils.MODAL);
				assertTrue(attTransactionModal.isVisible());
				
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
				attTransactionModal.locator(AddTransactionUtils.TO_BLOCK_SELECT).selectOption(toBlock.get("id").asText());
				
				attTransactionModal.locator(AddTransactionUtils.SUBMIT_BUTTON).click();
				MainAssertions.assertDoneProcessing(page1);
				
				return null;
			}
		);
		
		
	}
	
	
}
