package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types;

/**
 *
 * Sources on types of barcode:
 * https://eancheck.com/#barcode-12
 * https://www.computalabel.com/m/UPCexamplesM.htm
 *
 */
public enum IdentifierType {
	UPC_A,
	UPC_E,
	ISBN_10,
	ISBN_13,
	EAN_8,
	EAN_13,
	GTIN_14,
	
//	ASIN, Amazon specific, ignore
//	SKU, no way to determine proper format
	
	GENERIC(false, false),
	GENERATED(false, false),
	TO_GENERATE(false, false),
	;
	
	public final boolean isBarcode;
	public final boolean displayInBarcode;
	
	IdentifierType() {
		this.isBarcode = true;
		this.displayInBarcode = true;
	}
	
	IdentifierType(boolean isBarcode, boolean displayInBarcode) {
		this.isBarcode = isBarcode;
		this.displayInBarcode = displayInBarcode;
	}
	
	public String prettyName() {
		return this.name().replaceAll("_", "-");
	}
}
