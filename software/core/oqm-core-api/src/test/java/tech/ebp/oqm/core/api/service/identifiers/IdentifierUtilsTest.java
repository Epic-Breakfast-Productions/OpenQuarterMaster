package tech.ebp.oqm.core.api.service.identifiers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierUtilsTest extends CodeUtilTestBase {
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
		assertTrue(IdentifierUtils.isValidGenericId(code));
	}
	
	@ParameterizedTest
	@MethodSource("genericInvalid")
	public void isGenericIdInvalid(String code) {
		assertFalse(IdentifierUtils.isValidGenericId(code));
	}
	
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void isValidUPCACodeValid(String code, Identifier expectedId) {
		Identifier returned = IdentifierUtils.determineGeneralIdType(code);
		
		//this is fine, "determineGeneralId" doesn't set a label
		returned.setLabel(expectedId.getLabel());
		
		assertEquals(expectedId, returned);
	}
	
}