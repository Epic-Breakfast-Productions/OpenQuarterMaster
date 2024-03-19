package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemList;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class ItemListSearch extends SearchKeyAttObject<ItemList> {
	@QueryParam("name") String name;
	//TODO:: object specific fields, add to bson filter list
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.getName() != null && !this.getName().isBlank()) {
			filters.add(
				SearchUtils.getBasicSearchFilter("name", this.getName())
			);
		}
		
		return filters;
	}
}
