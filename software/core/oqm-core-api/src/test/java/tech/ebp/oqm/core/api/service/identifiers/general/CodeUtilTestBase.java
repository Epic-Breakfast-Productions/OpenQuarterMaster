package tech.ebp.oqm.core.api.service.identifiers.general;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_E;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class CodeUtilTestBase {
	
	protected static final List<String> UPCA_CODES = List.of(
		"012345678905",
		"078000003239",
		"063200009716",
		"022000159359",
		"887386978761"
	);
	
	protected static final List<String> UPCE_CODES = List.of(
		"02345673",
		"02345147"
	);
	
	protected static final List<String> ISBN13_CODES = List.of(
		"9780306406157",
		"9798886451740"
	);
	
	protected static final List<String> ISBN10_CODES = List.of(
		"0747532699"
	);
	
	protected static final List<String> EAN13_CODES = List.of(
		"0997253048258",
		"4001686310854"
	);
	protected static final List<String> EAN8_CODES = List.of(
		"12345670"
	);
	protected static final List<String> GTIN14_CODES = List.of(
		"10012345678902"
	);
	protected static final List<String> GENERIC_IDENTIFIERS = List.of(
		"Model T"
	);
	
	
	/**
	 * Gets a list of arguments comprised of the codes given.
	 *
	 * @param validCodes Codes to turn into arguments
	 *
	 * @return An argument stream from the codes given.
	 */
	protected static Stream<Arguments> validCodes(List<String> validCodes) {
		return validCodes.stream().map(Arguments::of);
	}
	
	/**
	 * Gets all codes available except the ones given
	 *
	 * @param validCodes The valid codes not to include
	 *
	 * @return A stream of all codes not in the provided list as arguments
	 */
	protected static Stream<Arguments> invalidCodes(List<String> validCodes) {
		return Stream.of(
				UPCA_CODES.stream(),
				UPCE_CODES.stream(),
				ISBN13_CODES.stream(),
				ISBN10_CODES.stream(),
				GTIN14_CODES.stream(),
				GENERIC_IDENTIFIERS.stream()
			).reduce(Stream::concat)
				   .orElseGet(Stream::empty)
				   .filter(s->!validCodes.contains(s))
				   .map(Arguments::of);
	}
	
	private static <T extends Identifier> Stream<Arguments> getArgs(List<String> codes, Function<String, T> generalBuilderFunc) {
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
				getArgs(GTIN14_CODES, (code)->GTIN_14.builder().value(code).build()),
				getArgs(GENERIC_IDENTIFIERS, (code)->GenericIdentifier.builder().value(code).build()),
				getArgs(UPCA_CODES, (code)->UPC_A.builder().label(IdentifierType.UPC_A.name()).value(code).build()),
				getArgs(UPCE_CODES, (code)->UPC_E.builder().label(IdentifierType.UPC_E.name()).value(code).build()),
				getArgs(ISBN13_CODES, (code)->ISBN_13.builder().label(IdentifierType.ISBN_13.name()).value(code).build()),
				getArgs(ISBN10_CODES, (code)->ISBN_10.builder().label(IdentifierType.ISBN_10.name()).value(code).build()),
				getArgs(EAN13_CODES, (code)->EAN_13.builder().label(IdentifierType.EAN_13.name()).value(code).build()),
				getArgs(EAN8_CODES, (code)->EAN_8.builder().label(IdentifierType.EAN_8.name()).value(code).build()),
				getArgs(GTIN14_CODES, (code)->GTIN_14.builder().label(IdentifierType.GTIN_14.name()).value(code).build()),
				getArgs(GENERIC_IDENTIFIERS, (code)->GenericIdentifier.builder().label(IdentifierType.GENERIC.name()).value(code).build()),
				getArgs(UPCA_CODES, (code)->UPC_A.builder().label("test").value(code).build()),
				getArgs(UPCE_CODES, (code)->UPC_E.builder().label("test").value(code).build()),
				getArgs(ISBN13_CODES, (code)->ISBN_13.builder().label("test").value(code).build()),
				getArgs(ISBN10_CODES, (code)->ISBN_10.builder().label("test").value(code).build()),
				getArgs(EAN13_CODES, (code)->EAN_13.builder().label("test").value(code).build()),
				getArgs(EAN8_CODES, (code)->EAN_8.builder().label("test").value(code).build()),
				getArgs(GTIN14_CODES, (code)->GTIN_14.builder().label("test").value(code).build()),
				getArgs(GENERIC_IDENTIFIERS, (code)->GenericIdentifier.builder().label("test").value(code).build())
			).reduce(Stream::concat)
				   .orElseGet(Stream::empty);
	}
	
}
