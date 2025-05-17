package tech.ebp.oqm.core.baseStation.testResources.ui.assertions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.NavUtils;

import java.util.List;

@Slf4j
public class MessageAssertions {
	public static String PAGE_MESSAGES = "#messageDiv";
	public static String MESSAGE = "div.alertMessage";
	public static String SUCCESS_MESSAGE = "success";
	
	
	
	
	public static void assertMessage(Locator messageContainer, String type, String titleText, String messageText){
		MainAssertions.assertDoneProcessing(messageContainer.page());
		List<Locator> messages = messageContainer.locator(MESSAGE).all();
		
		for (Locator message : messages) {
			boolean hasType = message.getAttribute("class").contains("alert-"+type);
			boolean hasTitle = (
				titleText != null &&
				titleText.equals(message.locator(".alert-heading-text").textContent().strip())
			);
			boolean hasMessage = (
				messageText != null &&
				messageText.equals(message.locator(".message").textContent().strip())
				);
			if(hasType && hasTitle && hasMessage){
				return;
			}
		}
		Assertions.fail("Failed to find expected message.");
	}
	
	public static void assertMessage(Page page, String type, String titleText, String messageText){
		assertMessage(page.locator(PAGE_MESSAGES), type, titleText, messageText);
	}
	
	
	
	
}
