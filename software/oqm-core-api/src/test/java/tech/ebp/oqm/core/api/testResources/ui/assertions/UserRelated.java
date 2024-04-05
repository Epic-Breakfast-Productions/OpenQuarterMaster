package tech.ebp.oqm.core.api.testResources.ui.assertions;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import tech.ebp.oqm.core.api.testResources.ui.WebDriverWrapper;
import tech.ebp.oqm.core.api.testResources.ui.pages.General;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;

import static org.junit.Assert.assertEquals;

@Slf4j
public class UserRelated {
	
	public static void assertUserLoggedIn(WebDriverWrapper wrapper, User testUser) {
		log.info("Asserting that test user {} is logged in.", testUser.getUsername());
		WebElement usernameDisplay = wrapper.waitFor(General.USERNAME_DISPLAY);
		assertEquals(testUser.getUsername(), usernameDisplay.getText());
	}
	
	public static void assertUserAdminLoggedIn(WebDriverWrapper wrapper, User testUser) {
		assertUserLoggedIn(wrapper, testUser);
		
		wrapper.waitFor(General.USERNAME_DISPLAY).click();
		
		wrapper.waitFor(General.USER_ADMIN_LINK);
	}
}
