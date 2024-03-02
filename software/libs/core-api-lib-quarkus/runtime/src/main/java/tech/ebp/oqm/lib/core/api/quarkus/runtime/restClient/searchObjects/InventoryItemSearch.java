package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject {
	@QueryParam("name") String name;
	@QueryParam("itemBarcode") String itemBarcode;
	@QueryParam("itemCategories") List<String> categories;
	@QueryParam("inStorageBlock") List<String> inStorageBlocks;
	
	//TODO:: object specific fields, add to bson filter list
	
}
