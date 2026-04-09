package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.PathParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
public class AppliedTransactionSearch extends SearchKeyAttObject<AppliedTransaction> {
	public static AppliedTransactionSearch newInstance(){
		return new AppliedTransactionSearch();
	}

	@PathParam("itemId")
	ObjectId inventoryItemId;

	//TODO:: More
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		if (hasValue(this.getInventoryItemId())) {
			filters.add(
				eq("inventoryItem", this.getInventoryItemId())
			);
		}
		
		return filters;
	}
}
