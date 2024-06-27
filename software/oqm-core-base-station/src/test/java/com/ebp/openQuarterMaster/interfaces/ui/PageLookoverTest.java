package com.ebp.openQuarterMaster.interfaces.ui;

import com.ebp.openQuarterMaster.testResources.TestUser;
import com.ebp.openQuarterMaster.testResources.testClasses.WebUiTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
public class PageLookoverTest extends WebUiTest {

	@Test
	public void testPageOverview() {
		TestUser testUser = this.getTestUserService().getTestUser();
		final Page page = this.getLoggedInPage(testUser);

	}

	//TODO:: page lookover test
}
