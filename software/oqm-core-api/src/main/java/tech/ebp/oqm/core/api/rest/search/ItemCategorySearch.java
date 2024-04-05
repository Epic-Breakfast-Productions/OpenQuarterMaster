package tech.ebp.oqm.core.api.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
public class ItemCategorySearch extends SearchObject<ItemCategory> {
	
	@QueryParam("name") String itemCategoryName;
	@QueryParam("isChildOf") ObjectId isChildOf;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.itemCategoryName)) {
			filters.add(SearchUtils.getBasicSearchFilter("name", this.itemCategoryName));
		}
		
		if(this.hasValue(this.isChildOf)){
			filters.add(eq("parent", this.isChildOf));
		}
		
		return filters;
	}
	
}
