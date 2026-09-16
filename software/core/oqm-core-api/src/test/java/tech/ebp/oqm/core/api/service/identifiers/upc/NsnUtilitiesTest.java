package tech.ebp.oqm.core.api.service.identifiers.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.CodeUtilTestBase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NsnUtilitiesTest extends CodeUtilTestBase {

	private static Stream<Arguments> nsnValidCodes() {
		return validCodes(NSN_CODES);
	}
	private static Stream<Arguments> nsnInvalidCodes() {
		return invalidCodes(NSN_CODES);
	}

	@ParameterizedTest
	@MethodSource("nsnValidCodes")
	public void isValidNsnCodeValid(String code) {
		assertTrue(NsnCodeUtilities.isValidNsnCode(code));
	}

	@ParameterizedTest
	@MethodSource("nsnInvalidCodes")
	public void isValidNsnCodeInvalid(String code) {
		assertFalse(NsnCodeUtilities.isValidNsnCode(code));
	}
}
