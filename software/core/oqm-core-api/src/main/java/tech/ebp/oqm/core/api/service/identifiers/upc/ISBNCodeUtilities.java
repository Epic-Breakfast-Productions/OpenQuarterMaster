package tech.ebp.oqm.core.api.service.identifiers.upc;

import lombok.NonNull;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;
import org.apache.commons.validator.routines.checkdigit.ISBN10CheckDigit;

/**
 * Class to handle common actions related to EAN (8 and 13) codes.
 * <p>
 * Sources:
 * <p>
 * <ul>
 *     <li>General Info: <a href="https://freeisbn.com/isbn-example/">https://freeisbn.com/isbn-example/</a></li>
 * </ul>
 */
public class ISBNCodeUtilities {
	
	private static final ISBN10CheckDigit VALIDATOR_10 = new ISBN10CheckDigit();
	private static final EAN13CheckDigit VALIDATOR_13 = new EAN13CheckDigit();
	
	/**
	 * Checks if an ISBN-13 code is valid.
	 *
	 * @param code
	 *
	 * @return
	 */
	public static boolean isValidISBN13Code(@NonNull String code) {
		if(code.length() != 13) {
			return false;
		}
		if (!(code.startsWith("978") || code.startsWith("979"))) {
			return false;
		}
		return VALIDATOR_13.isValid(code);
	}
	
	/**
	 * Checks if as ISBN-10 code is valid.
	 * <p>
	 * Sources:
	 * <p>
	 * <ul>
	 * </ul>
	 *
	 * @param code The code to check
	 *
	 * @return If it is a valid EAN-10 code.
	 */
	public static boolean isValidISBN10Code(@NonNull String code) {
		return VALIDATOR_10.isValid(code);
	}
	
	
}
