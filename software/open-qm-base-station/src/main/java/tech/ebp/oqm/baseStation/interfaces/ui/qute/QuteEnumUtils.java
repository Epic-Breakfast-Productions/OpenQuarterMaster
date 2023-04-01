package tech.ebp.oqm.baseStation.interfaces.ui.qute;

import io.quarkus.qute.TemplateData;
import tech.ebp.oqm.lib.core.object.itemList.ItemListActionMode;

@TemplateData
public class QuteEnumUtils {
	public static ItemListActionMode[] getItemListActionModes(){
		return ItemListActionMode.values();
	}
	
}
