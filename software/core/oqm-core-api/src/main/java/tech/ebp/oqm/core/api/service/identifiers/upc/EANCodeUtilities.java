package tech.ebp.oqm.core.api.service.identifiers.upc;

import lombok.NonNull;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

/**
 * Class to handle common actions related to EAN (8 and 13) codes.
 * <p>
 * Sources:
 * <p>
 * <ul>
 *     <li>General Info: <a href="https://www.gs1us.org/upcs-barcodes-prefixes/ean-vs-upc">https://www.gs1us.org/upcs-barcodes-prefixes/ean-vs-upc</a></li>
 * </ul>
 */
public class EANCodeUtilities {
	
	private static final EAN13CheckDigit VALIDATOR = new EAN13CheckDigit();
	
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
	public static boolean isValidEAN13Code(@NonNull String code) {
		if (code.length() != 13) {
			return false;
		}
//		if( code.charAt(0) != '0'){
//			return false;
//		}
		return VALIDATOR.isValid(code);
	}
	/**
	 *
	 * Sources:
	 * <ul>
	 *     <li><a href="https://en.wikipedia.org/wiki/Global_Trade_Item_Number#Format">https://en.wikipedia.org/wiki/Global_Trade_Item_Number#Format</a></li>
	 * </ul>
	 *
	 * @param ean8Code
	 *
	 * @return
	 */
	private static String getEAN13CodeFromEAN8Code(@NonNull String ean8Code) {
		return "00000" + ean8Code;
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
	public static boolean isValidEAN8Code(@NonNull String code) {
		if (code.length() != 8) {
			return false;
		}
		if( code.charAt(0) == '0'){
			return false;
		}
		String ean13Code = getEAN13CodeFromEAN8Code(code);
		
		return isValidEAN13Code(ean13Code);
	}
}
