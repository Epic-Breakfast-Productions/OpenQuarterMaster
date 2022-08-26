package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.object.storage.items.InventoryItem;
import lombok.Getter;
import lombok.ToString;

import javax.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	//TODO:: object specific fields, add to bson filter list
}
