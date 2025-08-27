package tech.ebp.oqm.core.api.service.codes;

import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.UPC_E;
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
		if(UpcCodeUtilities.isValidUPCECode(code)){
			return UPC_E.builder().value(code).build();
		}
		
		return Generic.builder().value(code).build();
	}
}
