package tech.ebp.oqm.lib.core.api.java;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OqmCoreApiClientTest {
	@Test
	void someLibraryMethodReturnsTrue() {
		OqmCoreApiClient classUnderTest = new OqmCoreApiClient();
		assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'");
	}
}