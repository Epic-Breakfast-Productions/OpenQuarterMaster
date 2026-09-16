package tech.ebp.oqm.core.api.service.identifiers.upc;

import lombok.NonNull;

/**
 * Class to handle common actions related to EAN (8 and 13) codes.
 * <p>
 * Sources:
 * <p>
 * <ul>
 *     <li>General Info: <a href="https://freeisbn.com/isbn-example/">https://freeisbn.com/isbn-example/</a></li>
 * </ul>
 */
public class NsnCodeUtilities {

	/**
	 * Checks if an NSN code is valid.
	 *
	 * @param code
	 *
	 * @return
	 */
	public static boolean isValidNsnCode(@NonNull String code) {
		if(code.length() != 13) {
			return false;
		}
		if(ISBNCodeUtilities.isValidISBN13Code(code)){
			return false;
		}
		return code.chars()
				   .allMatch(Character::isDigit);
	}
}
