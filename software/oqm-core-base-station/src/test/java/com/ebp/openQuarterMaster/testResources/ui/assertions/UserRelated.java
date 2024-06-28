package com.ebp.openQuarterMaster.testResources.ui.assertions;

import com.ebp.openQuarterMaster.testResources.testUsers.TestUser;
import com.ebp.openQuarterMaster.testResources.ui.WebDriverWrapper;
import com.ebp.openQuarterMaster.testResources.ui.pages.General;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class UserRelated {
	
	public static void assertUserLoggedIn(WebDriverWrapper wrapper, TestUser testUser) {
		log.info("Asserting that test user {} is logged in.", testUser.getUsername());
		WebElement usernameDisplay = wrapper.waitFor(General.USERNAME_DISPLAY);
		assertEquals(testUser.getUsername(), usernameDisplay.getText());
	}

	public static void assertUserAdminLoggedIn(WebDriverWrapper wrapper, TestUser testUser) {
		assertUserLoggedIn(wrapper, testUser);

		wrapper.waitFor(General.USERNAME_DISPLAY).click();

		wrapper.waitFor(General.USER_ADMIN_LINK);
	}
}
