package tech.ebp.oqm.core.api.service.identifiers.general;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_E;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GeneralIdUtilsTest extends CodeUtilTestBase {
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void isValidUPCACodeValid(String code, GeneralId expectedId) {
		GeneralId returned = GeneralIdUtils.determineGeneralIdType(code);
		
		assertEquals(expectedId, returned);
	}
	
}