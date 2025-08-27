package tech.ebp.oqm.core.api.service.codes.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UpcCodeUtilitiesTest {
	
	private static Stream<Arguments> upcaValidCodes() {
		return Stream.of(
			Arguments.of("012345678905"),
			Arguments.of("078000003239"),
			Arguments.of("063200009716"),
			Arguments.of("022000159359"),
			Arguments.of("887386978761")
		
		);
	}
	private static Stream<Arguments> upcaInvalidCodes() {
		return Stream.of(
			Arguments.of("012345678906")
		);
	}
	
	private static Stream<Arguments> upceValidCodes() {
		return Stream.of(
			Arguments.of("02345673"),
			Arguments.of("02345147")
		);
	}
	private static Stream<Arguments> upceInvalidCodes() {
		return Stream.of(
			Arguments.of("01234567"),
			Arguments.of("01234568")
		);
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