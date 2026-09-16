package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MessageAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class StorageBlockUiUtils {
	public static final String OBJ_KEY_LABEL = "label";
	private static final Set<String> blockNamesUsed = new HashSet<>();
	
	public static String newBlockName() {
		String output;
		do{
			output = WebServerTest.FAKER.location().building();
		} while (blockNamesUsed.contains(output));
		blockNamesUsed.add(output);
		return output;
	}
	
	public static void resetBlockNames(){
		blockNamesUsed.clear();
	}

	public static ObjectNode newStorageBlock(Page page, ObjectNode newBlock) {
		if (newBlock == null) {
			newBlock = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		}
		if (!newBlock.has(OBJ_KEY_LABEL)) {
			newBlock.put(OBJ_KEY_LABEL, newBlockName());
		}

		ObjectNode finalNewBlock = newBlock;
		NavUtils.performOnOtherPage(
			page,
			StorageBlockPage.STORAGE_PAGE,
			page1 -> {
				page1.locator(StorageBlockPage.MAIN_ADD_BUTTON).click();

				MainAssertions.assertDoneProcessing(page1);


				page1.locator(StorageBlockPage.ADDEDIT_FORM_LABEL_INPUT).fill(finalNewBlock.get(OBJ_KEY_LABEL).asText());
				//TODO:: other datas
//					oqm.locator(StorageBlockPage.ADDEDIT_FORM_NICKNAME_INPUT).fill(expectedNickname);
//					oqm.locator(StorageBlockPage.ADDEDIT_FORM_DESCRIPTION_INPUT).fill(expectedDescription);
//					oqm.locator(StorageBlockPage.ADDEDIT_FORM_LOCATION_INPUT).fill(expectedLocation);

				page1.locator(StorageBlockPage.ADDEDIT_FORM_SUBMIT_BUTTON).click();
				MainAssertions.assertDoneProcessing(page1);
				MessageAssertions.assertMessage(page1, MessageAssertions.SUCCESS_MESSAGE, "Success!", "Created storage block successfully!");

				//TODO:: find block in search, get id
				StorageBlockUiUtils.readBlockInfo(page1, finalNewBlock);

				return finalNewBlock;
			}
		);

		return newBlock;
	}

	public static ObjectNode readBlockInfo(Page page, ObjectNode blockInfo) {
		return NavUtils.performOnOtherPage(
			page,
			StorageBlockPage.STORAGE_PAGE,
			page1 -> {
				ObjectNode output = blockInfo;

				page1.locator(StorageBlockPage.SEARCH_RESULTS_TABLE)
					.locator("tbody")
					.locator(".blockSearchResultLabel")
					.getByText(blockInfo.get(OBJ_KEY_LABEL).asText(), new Locator.GetByTextOptions().setExact(false))
					.locator("..")
					.locator(".viewButton")
					.click();

				MainAssertions.assertDoneProcessing(page1);

				output.put("id", page1.locator(StorageBlockPage.VIEW_ID).textContent());
				output.put("labelText", page1.locator(StorageBlockPage.VIEW_MODAL_LABEL).textContent());

				log.info("Got info on storage block: {}", output);
				return output;
			}
		);
	}

	public static ObjectNode newStorageBlock(Page page) {
		return newStorageBlock(page, null);
	}
}
