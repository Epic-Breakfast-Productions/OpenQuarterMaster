package com.ebp.openQuarterMaster.plugin.restClients.searchObj;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItemSearch {
//	@QueryParam("keyword")
//	List<String> keywords;
//
//	@QueryParam("attributeKey") List<String> attributeKeys;
//	@QueryParam("attributeValue") List<String> attributeValues;
	
	@QueryParam("name") String name;
//	@QueryParam("itemBarcode") String itemBarcode;
	@QueryParam("itemCategories") List<String> categories;
}
