package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.lib.core.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@ToString(callSuper = true)
@Getter
public class ItemCheckoutSearch extends SearchKeyAttObject<ItemCheckout> {
	@QueryParam("item") String itemCheckedOut;
	@QueryParam("storage") String storageCheckedOutFrom;
	@QueryParam("entity") String checkedOutBy;
	@QueryParam("stillCheckedOut") boolean stillCheckedOut;
	
	
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
		
		//TODO:: these
		
		return filters;
	}
}
