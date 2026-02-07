package tech.ebp.oqm.core.api.service.identifiers.general;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedIdentifierUtilsTest extends CodeUtilTestBase {
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void isValidUPCACodeValid(String code, Identifier expectedId) {
		Identifier returned = GeneralIdUtils.determineGeneralIdType(code);
		
		//this is fine, "determineGeneralId" doesn't set a label
		returned.setLabel(expectedId.getLabel());
		
		assertEquals(expectedId, returned);
	}
	
}