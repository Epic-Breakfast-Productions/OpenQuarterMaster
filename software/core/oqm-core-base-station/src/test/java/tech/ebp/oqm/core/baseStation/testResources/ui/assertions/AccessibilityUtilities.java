package tech.ebp.oqm.core.baseStation.testResources.ui.assertions;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.microsoft.playwright.Page;

import java.util.List;

public class AccessibilityUtilities {

	private static final List<String> RULES_TO_EXCLUDE = List.of(
		"color-contrast"
	);

	public static AxeBuilder getAxeBuilder(Page page){
		return new AxeBuilder(page)
				   .disableRules(RULES_TO_EXCLUDE);
	}

}
