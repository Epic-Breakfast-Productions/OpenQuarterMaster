package tech.ebp.oqm.core.api.service.codes;

import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_E;
import tech.ebp.oqm.core.api.service.codes.upc.EANCodeUtilities;
import tech.ebp.oqm.core.api.service.codes.upc.GTINCodeUtilities;
import tech.ebp.oqm.core.api.service.codes.upc.ISBNCodeUtilities;
import tech.ebp.oqm.core.api.service.codes.upc.UpcCodeUtilities;


/**
 *
 * https://www.computalabel.com/m/UPCexamplesM.htm
 */
public class CodeUtils {
	
	public static GeneralId determineGeneralIdType(String code) {
		if(UpcCodeUtilities.isValidUPCACode(code)){
			return UPC_A.builder().value(code).build();
		}
		if(UpcCodeUtilities.isValidUPCECode(code)) {
			return UPC_E.builder().value(code).build();
		}
		if(ISBNCodeUtilities.isValidISBN13Code(code)) {
			return ISBN_13.builder().value(code).build();
		}
		if(ISBNCodeUtilities.isValidISBN10Code(code)) {
			return ISBN_10.builder().value(code).build();
		}
		if(EANCodeUtilities.isValidEAN13Code(code)) {
			return EAN_13.builder().value(code).build();
		}
		if(EANCodeUtilities.isValidEAN8Code(code)) {
			return EAN_8.builder().value(code).build();
		}
		if(GTINCodeUtilities.isValidGTIN14Code(code)) {
			return GTIN_14.builder().value(code).build();
		}
		
		return Generic.builder().value(code).build();
	}
}
