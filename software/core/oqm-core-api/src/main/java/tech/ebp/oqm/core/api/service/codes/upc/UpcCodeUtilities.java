package tech.ebp.oqm.core.api.service.codes.upc;

import lombok.NonNull;

/**
 * Class to handle common actions related to UPC (A & E) codes.
 * <p>
 * Sources:
 * <p>
 * <ul>
 *     <li>General Info: <a href="https://www.barcode-us.info/upc-codes/">https://www.barcode-us.info/upc-codes/</a></li>
 *     <li>Lookup Codes: <a href="https://www.barcodelookup.com/">https://www.barcodelookup.com/</a></li>
 *     <li>Converter: <a href="https://barcodeqrcode.com/convert-upc-e-to-upc-a/">https://barcodeqrcode.com/convert-upc-e-to-upc-a/</a></li>
 * </ul>
 */
public class UpcCodeUtilities {
	
	/**
	 * Checks if a UPC-A code is valid.
	 * <p>
	 * Sources:
	 * <p>
	 * <ul>
	 *     <li><a href="https://sps-support.honeywell.com/s/article/How-is-the-UPC-A-check-digit-calculated">https://sps-support.honeywell.com/s/article/How-is-the-UPC-A-check-digit-calculated</a></li>
	 *     <li><a href="https://www.quora.com/How-can-I-code-the-UPC-algorithm-for-checksum-digit-in-Java">https://www.quora.com/How-can-I-code-the-UPC-algorithm-for-checksum-digit-in-Java</a></li>
	 * </ul>
	 *
	 * @param code The code to check
	 *
	 * @return If it is a valid UPC-A code.
	 */
	public static boolean isValidUPCACode(@NonNull String code) {
		if (code.length() != 12) {
			return false;
		}
		
		int checkDigit = Character.getNumericValue(code.charAt(11));
		//indexes are -1 of what they should be
		int oddDigitSum = (
			Character.getNumericValue(code.charAt(0)) +
			Character.getNumericValue(code.charAt(2)) +
			Character.getNumericValue(code.charAt(4)) +
			Character.getNumericValue(code.charAt(6)) +
			Character.getNumericValue(code.charAt(8)) +
			Character.getNumericValue(code.charAt(10))
		);
		int evenDigitSum = (
			Character.getNumericValue(code.charAt(1)) +
			Character.getNumericValue(code.charAt(3)) +
			Character.getNumericValue(code.charAt(5)) +
			Character.getNumericValue(code.charAt(7)) +
			Character.getNumericValue(code.charAt(9))
		);
		
		int totalSum = ((oddDigitSum * 3) + evenDigitSum);
		int calculatedCheckDigit = (10 - (totalSum % 10)) % 10;
		
		return checkDigit == calculatedCheckDigit;
	}
	
	/**
	 *
	 * Sources:
	 * <ul>
	 *     <li>Java answer: <a href="https://stackoverflow.com/a/64144758">https://stackoverflow.com/a/64144758</a></li>
	 * </ul>
	 *
	 * @param upceCode
	 *
	 * @return
	 */
	private static String getUPCACodeFromUPCECode(@NonNull String upceCode) {
		String upcA;
		switch (upceCode.charAt(6)) {
			case '0':
			case '1':
			case '2': {
				upcA = upceCode.substring(0, 3) + upceCode.charAt(6) + "0000" + upceCode.substring(3, 6) + upceCode.charAt(7);
				break;
			}
			case '3': {
				upcA = upceCode.substring(0, 4) + "00000" + upceCode.substring(4, 6) + upceCode.charAt(7);
				break;
			}
			case '4': {
				upcA = upceCode.substring(0, 5) + "00000" + upceCode.charAt(5) + upceCode.charAt(7);
				break;
			}
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': {
				upcA = upceCode.substring(0, 6) + "0000" + upceCode.charAt(6) + upceCode.charAt(7);
				break;
			}
			default: {
				//shouldn't get here
				throw new IllegalArgumentException("Invalid UPC-E code: " + upceCode);
			}
		}
		return upcA;
	}
	
	/**
	 *
	 * Sources:
	 * <ul>
	 *     <li><a href="https://free-barcode.com/BarcodeOnline/UPCEOnline.asp">https://free-barcode.com/BarcodeOnline/UPCEOnline.asp</a></li>
	 *     <li>https://stackoverflow.com/a/64144758</li>
	 * </ul>
	 *
	 * @param code
	 *
	 * @return
	 */
	public static boolean isValidUPCECode(@NonNull String code) {
		if (code.length() != 8) {
			return false;
		}
		if (code.charAt(0) != '0') {
			return false;
		}
		
		String upcA = getUPCACodeFromUPCECode(code);
		
		return isValidUPCACode(upcA);
	}
}
