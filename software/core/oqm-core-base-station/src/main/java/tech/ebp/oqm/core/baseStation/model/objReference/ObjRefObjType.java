package tech.ebp.oqm.core.baseStation.model.objReference;

public enum ObjRefObjType {
	STORAGE_BLOCK("StorageBlock", "B", false),
	INVENTORY_ITEM("InventoryItem", "I", false),
	STORED("ItemStored", "S", true)
	;

	public final String longForm;
	public final String shortForm;
	public final boolean hasSecondId;

	ObjRefObjType(String longForm, String shortForm, boolean hasSecondId){
		this.longForm = longForm;
		this.shortForm = shortForm;
		this.hasSecondId = hasSecondId;
	}
}
