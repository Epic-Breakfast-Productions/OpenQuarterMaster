package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;

@ToString(callSuper = true)
@Getter
public class CategoriesSearch extends SearchObject<ItemCategory> {
	@QueryParam("name") String unitName;
	//TODO:: add to bson filter list
	
}
