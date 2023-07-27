package tech.ebp.oqm.baseStation.interfaces.ui.qute;

import io.quarkus.qute.TemplateData;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemListActionMode;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInType;

@TemplateData
public class QuteEnumUtils {
	public static ItemListActionMode[] getItemListActionModes(){
		return ItemListActionMode.values();
	}
	public static CheckInType[] getCheckinTypes(){
		return CheckInType.values();
	}
	
}
