package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@ToString(callSuper = true)
@Getter
@Setter
public class ItemCheckoutSearch extends SearchKeyAttObject<ItemCheckout> {
	@QueryParam("item") ObjectId itemCheckedOut;
	@QueryParam("storageCheckedOutFrom") ObjectId storageCheckedOutFrom;
	@QueryParam("entity") ObjectId checkedOutBy;
	@QueryParam("checkOutTransaction") ObjectId checkOutTransaction;
	@QueryParam("stillCheckedOut") Boolean stillCheckedOut = true;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (hasValue(this.getStillCheckedOut())) {
			filters.add(
				eq("stillCheckedOut", this.getStillCheckedOut())
			);
		}
		if (hasValue(this.getItemCheckedOut())) {
			filters.add(
				eq("item", this.getItemCheckedOut())
			);
		}
		if (hasValue(this.getStorageCheckedOutFrom())) {
			filters.add(
				eq("checkedOutFrom", this.getStorageCheckedOutFrom())
			);
		}
		if (hasValue(this.getCheckOutTransaction())) {
			filters.add(
				eq("checkOutTransaction", this.getCheckOutTransaction())
			);
		}
		
		//TODO:: checkedOutBy
		
		return filters;
	}
}
