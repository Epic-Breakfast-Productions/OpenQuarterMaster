package tech.ebp.oqm.core.api.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@ToString(callSuper = true)
@Getter
public class OqmMongoDbSearch extends SearchKeyAttObject<OqmMongoDatabase> {
	@QueryParam("nameOrDisplay") String nameOrDisplay;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.getNameOrDisplay())) {
			filters.add(
				or(
					SearchUtils.getBasicSearchFilter("name", this.getNameOrDisplay()),
					SearchUtils.getBasicSearchFilter("display", this.getNameOrDisplay())
				)
			);
		}
		
		return filters;
	}
}
