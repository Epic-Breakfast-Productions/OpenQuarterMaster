
package tech.ebp.oqm.core.api.service.identifiers.upc;

import lombok.NonNull;

/**
 * Class to handle GTIN codes not covered by more specific formats.
 */
public class GTINCodeUtilities {
	
	/**
	 * Checks if a EAN-13 code is valid.
	 * <p>
	 * Sources:
	 * <p>
	 * <ul>
	 * </ul>
	 *
	 * @param code The code to check
	 *
	 * @return If it is a valid EAN-13 code.
	 */
	public static boolean isValidGTIN14Code(@NonNull String code) {
		if (code.length() != 14) {
			return false;
		}
		
		int checkDigit = Character.getNumericValue(code.charAt(13));
		//indexes are -1 of what they should be
		int oddDigitSum = (
			Character.getNumericValue(code.charAt(0)) +
			Character.getNumericValue(code.charAt(2)) +
			Character.getNumericValue(code.charAt(4)) +
			Character.getNumericValue(code.charAt(6)) +
			Character.getNumericValue(code.charAt(8)) +
			Character.getNumericValue(code.charAt(10))+
			Character.getNumericValue(code.charAt(12))
		);
		int evenDigitSum = (
			Character.getNumericValue(code.charAt(1)) +
			Character.getNumericValue(code.charAt(3)) +
			Character.getNumericValue(code.charAt(5)) +
			Character.getNumericValue(code.charAt(7)) +
			Character.getNumericValue(code.charAt(9)) +
			Character.getNumericValue(code.charAt(11))
		);
		
		int totalSum = ((oddDigitSum * 3) + evenDigitSum);
		int calculatedCheckDigit = (10 - (totalSum % 10)) % 10;
		
		return checkDigit == calculatedCheckDigit;
		
	}
}
