package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	//TODO:: object specific fields, add to bson filter list
	
	@HeaderParam("accept") String acceptHeaderVal;
	//options for html rendering
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
}
