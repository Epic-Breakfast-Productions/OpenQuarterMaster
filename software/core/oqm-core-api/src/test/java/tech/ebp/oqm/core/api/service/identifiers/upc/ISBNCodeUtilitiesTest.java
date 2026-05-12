package tech.ebp.oqm.core.api.service.identifiers.upc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.identifiers.CodeUtilTestBase;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ISBNCodeUtilitiesTest extends CodeUtilTestBase {
	
	private static Stream<Arguments> isbn13ValidCodes() {
		return validCodes(ISBN13_CODES);
	}
	private static Stream<Arguments> isbn13InvalidCodes() {
		return invalidCodes(ISBN13_CODES);
	}
	
	private static Stream<Arguments> isbn10ValidCodes() {
		return validCodes(ISBN10_CODES);
	}
	private static Stream<Arguments> isbn10InvalidCodes() {
		return invalidCodes(ISBN10_CODES);
	}
	
	@ParameterizedTest
	@MethodSource("isbn13ValidCodes")
	public void isValidIsbn13CodeValid(String code) {
		assertTrue(ISBNCodeUtilities.isValidISBN13Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("isbn13InvalidCodes")
	public void isValidISBN13CodeInvalid(String code) {
		assertFalse(ISBNCodeUtilities.isValidISBN13Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("isbn10ValidCodes")
	public void isValidISBN10CodeValid(String code) {
		assertTrue(ISBNCodeUtilities.isValidISBN10Code(code));
	}
	
	@ParameterizedTest
	@MethodSource("isbn10InvalidCodes")
	public void isValidISBN10CodeInvalid(String code) {
		assertFalse(ISBNCodeUtilities.isValidISBN10Code(code));
	}
}