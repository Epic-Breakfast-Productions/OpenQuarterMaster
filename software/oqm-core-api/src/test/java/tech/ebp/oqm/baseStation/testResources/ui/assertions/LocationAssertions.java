package tech.ebp.oqm.baseStation.testResources.ui.assertions;

import tech.ebp.oqm.baseStation.testResources.ui.WebDriverWrapper;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class LocationAssertions {
	
	public static void assertOnPage(WebDriverWrapper wrapper, String endpoint) {
		try {
			assertEquals(endpoint, new URL(wrapper.getWebDriver().getCurrentUrl()).getPath());
		} catch(MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
