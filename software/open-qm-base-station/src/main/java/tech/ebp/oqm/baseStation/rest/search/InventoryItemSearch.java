package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;

import javax.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	//TODO:: object specific fields, add to bson filter list
}
