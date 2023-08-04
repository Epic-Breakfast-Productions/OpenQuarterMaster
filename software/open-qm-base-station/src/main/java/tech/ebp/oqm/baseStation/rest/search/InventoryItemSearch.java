package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	@QueryParam("itemBarcode") String itemBarcode;
	@QueryParam("itemCategories") List<ObjectId> categories;
	
	
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
		if (this.getCategories() != null && !this.categories.isEmpty()) {
			List<Bson> catsFilterList = new ArrayList<>(this.getCategories().size());
			for (ObjectId curCategoryId : this.getCategories()) {
				catsFilterList.add(in(
					"categories",
					curCategoryId
				));
			}
			filters.add(Filters.or(catsFilterList));
		}
		
		return filters;
	}
}
