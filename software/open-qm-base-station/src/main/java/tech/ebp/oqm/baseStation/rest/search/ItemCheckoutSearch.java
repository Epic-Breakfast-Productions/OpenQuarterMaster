package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.ItemCheckout;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
public class ItemCheckoutSearch extends SearchKeyAttObject<ItemCheckout> {
	@QueryParam("item") String itemCheckedOut;
	@QueryParam("storageCheckedOutFrom") String storageCheckedOutFrom;
	@QueryParam("entity") String checkedOutBy;
	@QueryParam("stillCheckedOut") Boolean stillCheckedOut = true;
	
	@HeaderParam("accept") String acceptHeaderVal;
	//options for html rendering
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	@HeaderParam("showItemCol") Boolean showItemCol = true;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.getStillCheckedOut())) {
			filters.add(
				eq("stillCheckedOut", this.getStillCheckedOut())
			);
		}
		if (this.hasValue(this.getItemCheckedOut())) {
			filters.add(
				eq("item", new ObjectId(this.getItemCheckedOut()))
			);
		}
		if (this.hasValue(this.getStorageCheckedOutFrom())) {
			filters.add(
				eq("checkedOutFrom", new ObjectId(this.getStorageCheckedOutFrom()))
			);
		}
		
		//TODO:: checkedOutBy
		
		return filters;
	}
}
