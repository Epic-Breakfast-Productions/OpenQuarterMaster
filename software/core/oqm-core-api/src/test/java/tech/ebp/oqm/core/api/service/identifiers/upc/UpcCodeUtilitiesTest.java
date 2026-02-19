package tech.ebp.oqm.core.api.service.identifiers.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.CodeUtilTestBase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UpcCodeUtilitiesTest extends CodeUtilTestBase {
	
	private static Stream<Arguments> upcaValidCodes() {
		return validCodes(UPCA_CODES);
	}
	private static Stream<Arguments> upcaInvalidCodes() {
		return invalidCodes(UPCA_CODES);
	}
	
	private static Stream<Arguments> upceValidCodes() {
		return validCodes(UPCE_CODES);
	}
	private static Stream<Arguments> upceInvalidCodes() {
		return invalidCodes(UPCE_CODES);
	}
	
	@ParameterizedTest
	@MethodSource("upcaValidCodes")
	public void isValidUPCACodeValid(String code) {
		assertTrue(UpcCodeUtilities.isValidUPCACode(code));
	}
	
	@ParameterizedTest
	@MethodSource("upcaInvalidCodes")
	public void isValidUPCACodeInvalid(String code) {
		assertFalse(UpcCodeUtilities.isValidUPCACode(code));
	}
	
	@ParameterizedTest
	@MethodSource("upceValidCodes")
	public void isValidUPCECodeValid(String code) {
		assertTrue(UpcCodeUtilities.isValidUPCECode(code));
	}
	
	@ParameterizedTest
	@MethodSource("upceInvalidCodes")
	public void isValidUPCECodeInvalid(String code) {
		assertFalse(UpcCodeUtilities.isValidUPCECode(code));
	}
}