package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import jakarta.ws.rs.core.Link;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.StorageBlockPage;
import tech.ebp.oqm.core.baseStation.utils.ObjectUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class AttKeywordUiUtils {
	public static final String KW_ADD_BUTTON = ".keywordAddButton";
	public static final String KW_INPUT_CONTAINER = ".keywordInputDiv";
	public static final String KW_INPUT = ".keywordInput";

	public static final String ATT_ADD_BUTTON = ".attributeAddButton";
	public static final String ATT_INPUT_CONTAINER = ".attInputDiv";
	public static final String ATT_KEY_INPUT = ".attInputKey";
	public static final String ATT_VAL_INPUT = ".attInputValue";


	public static final String KW_VIEW_CONTAINER = ".keywordsViewContainer";
	public static final String ATT_VIEW_CONTAINER = ".attsViewContainer";

	public static List<String> fillKeywords(Locator container, int numKeywords){
		Locator addButton = container.locator(KW_ADD_BUTTON);
		Locator inputContainer = container.locator(KW_INPUT_CONTAINER);
		List<String> keywords = new ArrayList<>(numKeywords);
		for(int i = 0; i < numKeywords; i++){
			String newKeyword = RunningServerTest.FAKER.funnyName().name();
			keywords.add(newKeyword);
			addButton.click();
			inputContainer.locator(KW_INPUT).last().fill(newKeyword);
		}

		return keywords;
	}
	public static List<String> fillKeywords(Locator container){
		return fillKeywords(container, 1);
	}

	public static Map<String, String> fillAtts(Locator container, int numAtts){
		Locator addButton = container.locator(ATT_ADD_BUTTON);
		Locator inputContainer = container.locator(ATT_INPUT_CONTAINER);
		Map<String, String> output = new LinkedHashMap<>(numAtts);
		for(int i = 0; i < numAtts; i++){
			String newKey = RunningServerTest.FAKER.funnyName().name();
			String newVal = RunningServerTest.FAKER.animal().name();
			output.put(newKey, newVal);
			addButton.click();
			inputContainer.locator(ATT_KEY_INPUT).last().fill(newKey);
			inputContainer.locator(ATT_VAL_INPUT).last().fill(newVal);
		}

		return output;
	}
	public static Map<String, String> fillAtts(Locator container){
		return fillAtts(container, 1);
	}

	public static void assertKeywordsViewVisible(Locator container){
		assertTrue(container.locator(KW_VIEW_CONTAINER).isVisible());
	}

	public static void assertKeywords(Locator container, List<String> keywordsExpected){
		assertKeywordsViewVisible(container);
		Locator viewContainer = container.locator(KW_VIEW_CONTAINER).locator("span");

		List<Locator> spans = viewContainer.all();
		assertEquals(
			keywordsExpected.size(),
			spans.size()
		);
		for(int i = 0; i < keywordsExpected.size(); i++){
			String curExpected = keywordsExpected.get(i);
			Locator curKw = spans.get(i);

			assertEquals(
				curExpected,
				curKw.textContent().strip()
			);
		}
	}

	public static void assertAttViewVisible(Locator container){
		assertTrue(container.locator(KW_VIEW_CONTAINER).isVisible());
	}
	public static void assertAtts(Locator container, Map<String, String> attsExpected){
		assertAttViewVisible(container);
		Locator viewContainer = container.locator(ATT_VIEW_CONTAINER);

		List<Locator> spans = viewContainer.locator("span.badge").all();
		assertEquals(
			attsExpected.size(),
			spans.size()
		);
		Map<String, String> left = new HashMap<>(attsExpected);
		for(Locator curAttSpan : spans){
			String curKey = curAttSpan.locator(".attKey").textContent().strip();
			String curVal = curAttSpan.locator(".attVal").textContent().strip();

			assertTrue(left.containsKey(curKey));
			assertEquals(
				left.get(curKey),
				curVal
			);

			left.remove(curKey);
		}
		assertTrue(left.isEmpty());
	}
}
