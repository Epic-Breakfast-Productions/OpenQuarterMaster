package tech.ebp.oqm.core.api.service.identifiers;

import lombok.NonNull;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_E;
import tech.ebp.oqm.core.api.service.identifiers.upc.EANCodeUtilities;
import tech.ebp.oqm.core.api.service.identifiers.upc.GTINCodeUtilities;
import tech.ebp.oqm.core.api.service.identifiers.upc.ISBNCodeUtilities;
import tech.ebp.oqm.core.api.service.identifiers.upc.UpcCodeUtilities;


/**
 *
 * https://www.computalabel.com/m/UPCexamplesM.htm
 */
public class IdentifierUtils {
	
	public static Identifier determineGeneralIdType(String code) {
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
		if(isValidGenericId(code)) {
			return GenericIdentifier.builder().value(code).build();
		}
		
		throw new IllegalArgumentException("Code given does not fit into any category of id.");
	}
	
	public static boolean isValidCode(IdentifierType type, String code) {
		switch (type){
			case UPC_A -> {
				return UpcCodeUtilities.isValidUPCACode(code);
			}
			case UPC_E -> {
				return UpcCodeUtilities.isValidUPCECode(code);
			}
			case ISBN_10 -> {
				return ISBNCodeUtilities.isValidISBN10Code(code);
			}
			case ISBN_13 -> {
				return ISBNCodeUtilities.isValidISBN13Code(code);
			}
			case EAN_8 -> {
				return EANCodeUtilities.isValidEAN8Code(code);
			}
			case EAN_13 -> {
				return EANCodeUtilities.isValidEAN13Code(code);
			}
			case GTIN_14 -> {
				return GTINCodeUtilities.isValidGTIN14Code(code);
			}
			case GENERIC -> {
				return isValidGenericId(code);
			}
		}
		throw new IllegalArgumentException("Unknown id type: " + type);
	}
	
	public static Identifier objFromParts(IdentifierType type, String code) {
		switch (type){
			case UPC_A -> {
				if(UpcCodeUtilities.isValidUPCACode(code)){
					return UPC_A.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid UPC-A code: " + code);
			}
			case UPC_E -> {
				if(UpcCodeUtilities.isValidUPCECode(code)) {
					return UPC_E.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid UPC-E code: " + code);
			}
			case ISBN_10 -> {
				if(ISBNCodeUtilities.isValidISBN10Code(code)) {
					return ISBN_10.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid ISBN-10 code: " + code);
			}
			case ISBN_13 -> {
				if(ISBNCodeUtilities.isValidISBN13Code(code)) {
					return ISBN_13.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid ISBN-13 code: " + code);
			}
			case EAN_8 -> {
				if(EANCodeUtilities.isValidEAN8Code(code)) {
					return EAN_8.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid EAN-8 code: " + code);
			}
			case EAN_13 -> {
				if(EANCodeUtilities.isValidEAN13Code(code)) {
					return EAN_13.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid EAN-13 code: " + code);
			}
			case GTIN_14 -> {
				if(GTINCodeUtilities.isValidGTIN14Code(code)) {
					return GTIN_14.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid GTIN-14 code: " + code);
			}
			case GENERIC -> {
				if(isValidGenericId(code)) {
					return GenericIdentifier.builder().value(code).build();
				}
				throw new IllegalArgumentException("Invalid generic code: " + code);
			}
		}
		throw new IllegalArgumentException("Unknown id type: " + type);
	}
	
	public static boolean isValidGenericId(@NonNull String id) {
		return !id.isBlank();
	}
}
