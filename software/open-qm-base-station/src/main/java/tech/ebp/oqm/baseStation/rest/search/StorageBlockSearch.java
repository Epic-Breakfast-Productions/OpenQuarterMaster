package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

import javax.measure.Quantity;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.util.List;

@ToString(callSuper = true)
@Getter
public class StorageBlockSearch extends SearchKeyAttObject<StorageBlock> {
	public static StorageBlockSearch newInstance(){
		return new StorageBlockSearch();
	}
	
	//for actual queries
	@QueryParam("label") String label;
	@QueryParam("location") String location;
	@QueryParam("parentLabel")
	List<String> parents;
	//capacities
	@QueryParam("capacity") List<Integer> capacities;
	@QueryParam("unit") List<String> units;
//	@QueryParam("stores") List<ObjectId> stores; //TODO: need aggregate?
	@QueryParam("parent") ObjectId parent; //TODO
	
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
