package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import javax.measure.Quantity;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.in;

@ToString(callSuper = true)
@Getter
public class StorageBlockSearch extends SearchKeyAttObject<StorageBlock> {
	public static StorageBlockSearch newInstance(){
		return new StorageBlockSearch();
	}
	
	//for actual queries
	@QueryParam("labelOrNickname") String labelOrNickname;
	@QueryParam("location") String location;
	@QueryParam("storedCategories") List<ObjectId> categories;
	@QueryParam("parentLabel")
	List<String> parents;
	//	@QueryParam("stores") List<ObjectId> stores; //TODO: need aggregate?
	@QueryParam("parent") ObjectId parent; //TODO:
	//capacities
	@QueryParam("capacity") List<Integer> capacities;//TODO
	@QueryParam("unit") List<String> units;//TODO
	
	@HeaderParam("accept") String acceptHeaderVal;
	//options for html rendering
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.getLabelOrNickname() != null && !this.getLabelOrNickname().isBlank()) {
			filters.add(
				Filters.or(
					SearchUtils.getBasicSearchFilter("label", this.getLabelOrNickname()),
					SearchUtils.getBasicSearchFilter("nickname", this.getLabelOrNickname())
				)
			);
		}
		SearchUtils.addBasicSearchFilter(filters, "location", this.getLocation());
		
		if (this.getCategories() != null && !this.categories.isEmpty()) {
			List<Bson> catsFilterList = new ArrayList<>(this.getCategories().size());
			for (ObjectId curCategoryId : this.getCategories()) {
				catsFilterList.add(in(
					"storedCategories",
					curCategoryId
				));
			}
			filters.add(Filters.or(catsFilterList));
		}
		
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
