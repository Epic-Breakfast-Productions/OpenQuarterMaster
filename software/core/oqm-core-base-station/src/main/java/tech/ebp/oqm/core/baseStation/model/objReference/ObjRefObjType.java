package tech.ebp.oqm.core.baseStation.model.objReference;

public enum ObjRefObjType {
	StorageBlock("StorageBlock", "B", false),
	InventoryItem("InventoryItem", "I", false),
	Stored("Stored", "S", true)
	;

	public final String longForm;
	public final String shortForm;
	public final boolean hasSecondId;

	ObjRefObjType(String longForm, String shortForm, boolean hasSecondId){
		this.longForm = longForm;
		this.shortForm = shortForm;
		this.hasSecondId = hasSecondId;
	}

	public static ObjRefObjType toRef(String typeString){
		for(ObjRefObjType cur : ObjRefObjType.values()){
			if(
				cur.longForm.equalsIgnoreCase(typeString) ||
				cur.shortForm.equalsIgnoreCase(typeString)
			){
				return cur;
			}
		}
		throw new IllegalArgumentException("Invalid obj ref object type: " + typeString);
	}
}
