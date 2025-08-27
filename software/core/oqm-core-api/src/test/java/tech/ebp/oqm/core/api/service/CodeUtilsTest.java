package tech.ebp.oqm.core.api.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.UPC_E;
import tech.ebp.oqm.core.api.service.codes.CodeUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class CodeUtilsTest {
	
	private static Stream<Arguments> codes() {
		return Stream.of(
			Arguments.of("012345678905", UPC_A.builder().value("012345678905").build()),
			Arguments.of("02345673", UPC_E.builder().value("02345673").build()),
			
			Arguments.of("foo", Generic.builder().value("foo").build())
		);
	}
	
	@ParameterizedTest
	@MethodSource("codes")
	public void testValidCodes(String code, GeneralId expectedId) {
		GeneralId result = CodeUtils.determineGeneralIdType(code);
		
		assertEquals(expectedId, result);
	}
}