package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MessageAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ItemsUiUtils {

	public static Stream<Arguments> itemTypeArgs(){
		return Stream.of(
			Arguments.of("BULK"),
			Arguments.of("AMOUNT_LIST"),
			Arguments.of("UNIQUE_MULTI"),
			Arguments.of("UNIQUE_SINGLE")
		);
	}

	public static final String OBJ_KEY_LABEL = "label";
	public static final String OBJ_KEY_TYPE = "type";

	public static ObjectNode newItem(Page page, ObjectNode newItem, List<ObjectNode> storageBlocks) {
		if (newItem == null) {
			newItem = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		}
		if (!newItem.has(OBJ_KEY_LABEL)) {
			newItem.put(OBJ_KEY_LABEL, WebServerTest.FAKER.appliance().equipment());
		}

		ObjectNode finalNewItem = newItem;
		NavUtils.performOnOtherPage(
			page,
			ItemsPage.ITEMS_PAGE,
			page1 -> {
				page1.locator(ItemsPage.MAIN_ADD_BUTTON).click();
				MainAssertions.assertDoneProcessing(page1);

				page1.locator(ItemsPage.ADDEDIT_FORM_INPUT_NAME).fill(finalNewItem.get(OBJ_KEY_LABEL).asText());
				if(finalNewItem.has(OBJ_KEY_TYPE)){
					page1.locator(ItemsPage.ADDEDIT_FORM_INPUT_TYPE).selectOption(finalNewItem.get(OBJ_KEY_TYPE).asText());
				}
				//TODO:: other datas

				for (ObjectNode storageBlock : storageBlocks) {
					log.info("Adding storage block: {}", storageBlock);
					StorageSearchSelectUiUtils.select(
						storageBlock,
						page1,
						ItemsPage.ADDEDIT_FORM_ADD_STORAGE_BUTTON
					);
				}
				
				List<Locator> associatedStorage = page1.locator(ItemsPage.ADDEDIT_FORM_STORAGE_CONTAINER).locator("div.blockSelection").all();
				assertEquals(
					storageBlocks.stream().map(block->block.get("id").asText()).toList(),
					associatedStorage.stream().map(locator->locator.getAttribute("data-block-id")).toList()
				);
				try {
					Thread.sleep(250);
				} catch(InterruptedException e) {
					throw new RuntimeException(e);
				}
				
				page1.locator(ItemsPage.ADD_EDIT_FORM_SUBMIT_BUTTON).click();
				MainAssertions.assertDoneProcessing(page1);
				MessageAssertions.assertMessage(page1, MessageAssertions.SUCCESS_MESSAGE, "Success!", "Created item successfully!");

				ItemsUiUtils.readItemInfo(page1, finalNewItem);

				return finalNewItem;
			}
		);

		return newItem;
	}

	public static ObjectNode newItem(Page page, List<ObjectNode> storageBlocks) {
		return newItem(page, null, storageBlocks);
	}
	
	public static String getViewItemEndpoint(ObjectNode itemInfo){
		return ItemsPage.ITEMS_PAGE + "?view=" + itemInfo.get("id").asText();
	}

	public static void viewItem(Page page, ObjectNode itemInfo){
		NavUtils.navigateToEndpoint(page, getViewItemEndpoint(itemInfo));
		Locator viewName = page.locator(ItemsPage.VIEW_NAME);
		assertTrue(viewName.isVisible());
		assertEquals(
			itemInfo.get(OBJ_KEY_LABEL).asText(),
			viewName.textContent().strip()
		);
	}

	public static ObjectNode readItemInfo(Page page, ObjectNode itemInfo) {
		return NavUtils.performOnOtherPage(
			page,
			ItemsPage.ITEMS_PAGE,
			page1 -> {
				page1.locator(ItemsPage.SEARCH_RESULTS_TABLE)
					.locator("tbody")
					.locator(".itemResultsName")
					.getByText(itemInfo.get(OBJ_KEY_LABEL).asText(), new Locator.GetByTextOptions().setExact(false))
					.locator("..")
					.locator(".viewButton")
					.click();

				MainAssertions.assertDoneProcessing(page1);

				itemInfo.put("id", page1.locator(ItemsPage.VIEW_ID).textContent());

				log.info("Got info on item: {}", itemInfo);
				return itemInfo;
			}
		);
	}
}
