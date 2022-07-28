package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.List;

@ToString(callSuper = true)
@Getter
public class StorageBlockSearch extends SearchKeyAttObject<StorageBlock> {
	//for actual queries
	@QueryParam("label") String label;
	@QueryParam("location") String location;
	@QueryParam("parentLabel")
	List<String> parents;
	//capacities
	@QueryParam("capacity") List<Integer> capacities;
	@QueryParam("unit") List<String> units;
	@QueryParam("stores") List<ObjectId> stores;
	@QueryParam("storedType")
	StoredType storedType;
	
	@HeaderParam("accept") String acceptHeaderVal;
	//options for html rendering
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		SearchUtils.addBasicSearchFilter(filters, "label", this.getLabel());
		SearchUtils.addBasicSearchFilter(filters, "location", this.getLocation());
		
		if (parents != null) {
			for (String curParentLabel : this.getParents()) {
				//TODO::parent labels
			}
		}
		
		if (capacities != null) {
			List<Quantity<?>> capacityList = SearchUtils.capacityListsToMap(capacities, units);
			for (Quantity<?> curCap : capacityList) {
				//TODO:: capacities with greater than or equal capacity to what was given
			}
		}
		
		//TODO:: stores
		
		return filters;
	}
	
	//TODO:: add to bson filter list
}
