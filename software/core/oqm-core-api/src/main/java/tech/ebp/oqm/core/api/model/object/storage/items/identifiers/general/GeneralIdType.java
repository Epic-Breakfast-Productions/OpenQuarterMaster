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
	EAN,
	EAN_8,
	EAN_13,
	GTIN_14,
	ASIN,
	SKU,
	ISBN_10,
	ISBN_13,
	
	GENERIC(false);
	
	public final boolean isBarcode;
	
	GeneralIdType() {
		this.isBarcode = true;
	}
	
	GeneralIdType(boolean isBarcode) {
		this.isBarcode = isBarcode;
	}
}
