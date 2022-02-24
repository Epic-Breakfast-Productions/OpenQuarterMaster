package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class, restrictToAnnotatedClass = true)
public class PageLookOverTest extends WebUiTest {
	
	@Inject
	TestUserService testUserService;
	
	@Inject
	WebDriverWrapper webDriverWrapper;
	
	private static final String[] PRE_LOGIN_PAGES = new String[]{
		"/accountCreate"
	};
	
	private static final String[] LOGGED_IN_PAGES = new String[]{
		"/overview",
		"/images",
		"/storage",
		"/items"
	};
	
	/**
	 * Sanity-check type test to prove all pages load
	 */
	@Test
	public void testPages() {
		User testUser = this.testUserService.getTestUser(true, true);
		
		for (String curPage : PRE_LOGIN_PAGES) {
			log.info("Attempting page: {}", curPage);
			this.webDriverWrapper.goTo(curPage);
			this.webDriverWrapper.waitForPageLoad();
		}
		
		this.webDriverWrapper.loginUser(testUser);
		
		for (String curPage : LOGGED_IN_PAGES) {
			log.info("Attempting page: {}", curPage);
			this.webDriverWrapper.goTo(curPage);
			this.webDriverWrapper.waitForPageLoad();
		}
	}
}
