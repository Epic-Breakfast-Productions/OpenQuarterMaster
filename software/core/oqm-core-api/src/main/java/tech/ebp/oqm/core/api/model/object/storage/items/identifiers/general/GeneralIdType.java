package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

/**
 *
 * Sources on types of barcode:
 * https://eancheck.com/#barcode-12
 * https://www.computalabel.com/m/UPCexamplesM.htm
 *
 */
public enum GeneralIdType {
	UPC_A,
	UPC_E,
	ISBN_10,
	ISBN_13,
	EAN_8,
	EAN_13,
	GTIN_14,
	
//	ASIN, Amazon specific, ignore
//	SKU, no way to determine proper format
	
	GENERIC(false);
	
	public final boolean isBarcode;
	
	GeneralIdType() {
		this.isBarcode = true;
	}
	
	GeneralIdType(boolean isBarcode) {
		this.isBarcode = isBarcode;
	}
}
