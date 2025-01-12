package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.PathParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class AppliedTransactionSearch extends SearchKeyAttObject<AppliedTransaction> {
	public static AppliedTransactionSearch newInstance(){
		return new AppliedTransactionSearch();
	}

	@PathParam("itemId") String inventoryItemId;

	//TODO:: More
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		if (this.hasValue(this.getInventoryItemId())) {
			filters.add(
				SearchUtils.getBasicSearchFilter("inventoryItem", this.getInventoryItemId())
			);
		}
		
		return filters;
	}
}
