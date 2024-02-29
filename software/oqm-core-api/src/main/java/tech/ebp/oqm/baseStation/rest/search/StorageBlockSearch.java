package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;

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
	@QueryParam("isParent") Boolean isParent = false;
	@QueryParam("isChild") Boolean isChild = false;
	@QueryParam("isChildOf") ObjectId isChildOf;
	//capacities
	@QueryParam("capacity") List<Integer> capacities;//TODO
	@QueryParam("unit") List<String> units;//TODO
	//Other special use cases
	@QueryParam("blocks") List<ObjectId> blocks;
	
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
		
		if (this.isParent) {
			filters.add(eq("parent", null));
		}
		if(this.isChild || this.hasValue(this.isChildOf)){
			if(this.hasValue(this.isChildOf)){
				filters.add(eq("parent", this.isChildOf));
			} else {
				filters.add(ne("parent", null));
			}
		}
		
		//TODO:: stores
		
		return filters;
	}
	
	//TODO:: add to bson filter list
}
