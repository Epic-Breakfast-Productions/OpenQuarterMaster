package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class ItemCategorySearch extends SearchObject {
	@QueryParam("name") String itemCategoryName;
	@QueryParam("isChildOf") String isChildOf;
}
