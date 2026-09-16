package tech.ebp.oqm.core.api.service.identifiers.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.CodeUtilTestBase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EANCodeUtilitiesTest extends CodeUtilTestBase {
	
	private static Stream<Arguments> ean13ValidCodes() {
		return validCodes(EAN13_CODES);
	}
	private static Stream<Arguments> ean13InvalidCodes() {
		return invalidCodes(
			// ISBN-13 are also valid EAN-13 codes: https://www.activebarcode.com/codes/isbn
			Stream.of(
				EAN13_CODES.stream(),
				ISBN13_CODES.stream()
			).reduce(Stream::concat)
				.orElseGet(Stream::empty)
				.toList()
		);
	}
	
	private static Stream<Arguments> ean8ValidCodes() {
		return validCodes(EAN8_CODES);
	}
	private static Stream<Arguments> ean8InvalidCodes() {
		return invalidCodes(EAN8_CODES);
	}
	
	@ParameterizedTest
	@MethodSource("ean13ValidCodes")
	public void isValidEAN13CodeValid(String code) {
		assertTrue(EANCodeUtilities.isValidEAN13Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("ean13InvalidCodes")
	public void isValidEAN13CodeInvalid(String code) {
		assertFalse(EANCodeUtilities.isValidEAN13Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("ean8ValidCodes")
	public void isValidEAN8CodeValid(String code) {
		assertTrue(EANCodeUtilities.isValidEAN8Code(code));
	}

	@ParameterizedTest
	@MethodSource("ean8InvalidCodes")
	public void isValidEAN8CodeInvalid(String code) {
		assertFalse(EANCodeUtilities.isValidEAN8Code(code));
	}
}