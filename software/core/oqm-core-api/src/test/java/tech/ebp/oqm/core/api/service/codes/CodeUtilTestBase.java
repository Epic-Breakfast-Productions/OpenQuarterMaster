package tech.ebp.oqm.core.api.service.codes;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
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
	
	
	/**
	 * Gets a list of arguments comprised of the codes given.
	 * @param validCodes Codes to turn into arguments
	 * @return An argument stream from the codes given.
	 */
	protected static Stream<Arguments> validCodes(List<String> validCodes) {
		return validCodes.stream().map(Arguments::of);
	}
	
	/**
	 * Gets all codes available except the ones given
	 * @param validCodes The valid codes not to include
	 * @return A stream of all codes not in the provided list as arguments
	 */
	protected static Stream<Arguments> invalidCodes(List<String> validCodes) {
		return Stream.of(
				UPCA_CODES.stream(),
				UPCE_CODES.stream(),
				ISBN13_CODES.stream(),
				ISBN10_CODES.stream(),
				GTIN14_CODES.stream()
			).reduce(Stream::concat)
				   .orElseGet(Stream::empty)
				   .filter(s->!validCodes.contains(s))
				   .map(Arguments::of);
	}
	
}
