package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	@QueryParam("itemBarcode") String itemBarcode;
	
	
	//TODO:: object specific fields, add to bson filter list
	
	@HeaderParam("accept") String acceptHeaderVal;
	//options for html rendering
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.getName())) {
			filters.add(
				SearchUtils.getBasicSearchFilter("name", this.getName())
			);
		}
		if (this.hasValue(this.getItemBarcode())) {
			filters.add(
				eq("barcode", this.getItemBarcode())
			);
		}
		
		return filters;
	}
}
