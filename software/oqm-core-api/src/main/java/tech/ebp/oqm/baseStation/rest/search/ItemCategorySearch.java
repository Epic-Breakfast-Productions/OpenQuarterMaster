package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

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
