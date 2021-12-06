package com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions;

import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.General;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import static com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.General.USER_ADMIN_LINK;
import static org.junit.Assert.assertEquals;

@Slf4j
public class UserRelated {

    public static void assertUserLoggedIn(WebDriverWrapper wrapper, User testUser){
        WebElement usernameDisplay = wrapper.waitFor(General.USERNAME_DISPLAY);

        assertEquals(testUser.getUsername(), usernameDisplay.getText());
    }

    public static void assertUserAdminLoggedIn(WebDriverWrapper wrapper, User testUser){
        assertUserLoggedIn(wrapper, testUser);

        wrapper.waitFor(General.USERNAME_DISPLAY).click();

        wrapper.waitFor(USER_ADMIN_LINK);
    }
}
