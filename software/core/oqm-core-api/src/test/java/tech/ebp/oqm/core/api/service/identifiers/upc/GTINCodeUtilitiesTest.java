package tech.ebp.oqm.core.api.service.identifiers.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.CodeUtilTestBase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GTINCodeUtilitiesTest extends CodeUtilTestBase {
	
	private static Stream<Arguments> gtin14ValidCodes() {
		return validCodes(GTIN14_CODES);
	}
	private static Stream<Arguments> gtin14InvalidCodes() {
		return invalidCodes(
			Stream.of(
				GTIN14_CODES.stream()
			).reduce(Stream::concat)
				.orElseGet(Stream::empty)
				.toList()
		);
	}
	
	@ParameterizedTest
	@MethodSource("gtin14ValidCodes")
	public void isValidGTIN14CodeValid(String code) {
		assertTrue(GTINCodeUtilities.isValidGTIN14Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("gtin14InvalidCodes")
	public void isValidGTIN14CodeInvalid(String code) {
		assertFalse(GTINCodeUtilities.isValidGTIN14Code(code));
	}
	
}