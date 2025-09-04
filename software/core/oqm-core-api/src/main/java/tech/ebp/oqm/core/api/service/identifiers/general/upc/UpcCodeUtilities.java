package tech.ebp.oqm.core.api.service.identifiers.general.upc;

import lombok.NonNull;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

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
	
	private static final EAN13CheckDigit VALIDATOR = new EAN13CheckDigit();
	
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
		return VALIDATOR.isValid(code);
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
