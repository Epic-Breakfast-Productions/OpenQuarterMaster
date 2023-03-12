package tech.ebp.oqm.baseStation.interfaces.ui.qute;

import io.quarkus.qute.TemplateData;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.rest.search.UserSearch;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityType;

@TemplateData
class QuteSearchUtils {
	public static HistorySearch newHistorySearchInstance(){
		return new HistorySearch();
	}
	public static ImageSearch newImageSearchInstance(){
		return new ImageSearch();
	}
	public static InventoryItemSearch newInventoryItemSearchInstance(){
		return new InventoryItemSearch();
	}
	public static StorageBlockSearch newStorageBlockSearchInstance(){
		return new StorageBlockSearch();
	}
	public static UserSearch newUserSearchInstance(){
		return new UserSearch();
	}
	
	public static boolean historyEntityIsBaseStation(InteractingEntityReference ref){
		return ref.getEntityType().equals(InteractingEntityType.BASE_STATION);
	}
	
}
