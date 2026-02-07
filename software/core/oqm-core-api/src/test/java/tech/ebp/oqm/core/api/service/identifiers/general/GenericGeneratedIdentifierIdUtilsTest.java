package tech.ebp.oqm.core.api.service.identifiers.general;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.general.upc.UpcCodeUtilities;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GenericGeneratedIdentifierIdUtilsTest extends CodeUtilTestBase {
	private static Stream<Arguments> genericValid() {
		return validCodes(GENERIC_IDENTIFIERS);
	}
	private static Stream<Arguments> genericInvalid() {
		return Stream.of(
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of("\n"),
			Arguments.of("\t")
		);
	}
	
	
	@ParameterizedTest
	@MethodSource("genericValid")
	public void isGenericIdValid(String code) {
		assertTrue(GenericIdUtils.isValidGenericId(code));
	}
	
	@ParameterizedTest
	@MethodSource("genericInvalid")
	public void isGenericIdInvalid(String code) {
		assertFalse(GenericIdUtils.isValidGenericId(code));
	}
	
}