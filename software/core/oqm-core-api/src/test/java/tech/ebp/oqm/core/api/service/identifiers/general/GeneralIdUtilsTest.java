package tech.ebp.oqm.core.api.service.identifiers.general;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
	
	private static <T extends GeneralId> Stream<Arguments> getArgs(List<String> codes, Function<String, T> generalBuilderFunc) {
		return codes.stream().map(code->Arguments.of(code, generalBuilderFunc.apply(code)));
	}
	
	public static Stream<Arguments> getCodes() {
		return Stream.of(
				getArgs(UPCA_CODES, (code)->UPC_A.builder().value(code).build()),
				getArgs(UPCE_CODES, (code)->UPC_E.builder().value(code).build()),
				getArgs(ISBN13_CODES, (code)->ISBN_13.builder().value(code).build()),
				getArgs(ISBN10_CODES, (code)->ISBN_10.builder().value(code).build()),
				getArgs(EAN13_CODES, (code)->EAN_13.builder().value(code).build()),
				getArgs(EAN8_CODES, (code)->EAN_8.builder().value(code).build()),
				getArgs(GTIN14_CODES, (code)->GTIN_14.builder().value(code).build())
			).reduce(Stream::concat)
				   .orElseGet(Stream::empty);
	}
	
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void isValidUPCACodeValid(String code, GeneralId expectedId) {
		GeneralId returned = GeneralIdUtils.determineGeneralIdType(code);
		
		assertEquals(expectedId, returned);
	}
	
}