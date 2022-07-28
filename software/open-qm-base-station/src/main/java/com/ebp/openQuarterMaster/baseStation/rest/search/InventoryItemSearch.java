package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	//TODO:: object specific fields, add to bson filter list
}
