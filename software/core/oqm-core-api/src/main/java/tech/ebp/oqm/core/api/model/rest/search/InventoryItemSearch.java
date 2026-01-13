package tech.ebp.oqm.core.api.model.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@ToString(callSuper = true)
@Getter
@Setter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	@QueryParam("storageTypes") List<StorageType> storageTypes;
	@QueryParam("itemCategories") List<ObjectId> categories;
	@QueryParam("inStorageBlock") List<ObjectId> inStorageBlocks;
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasNoExpired") Boolean hasNoExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasNoExpiryWarn") Boolean hasNoExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
	@QueryParam("hasNoLowStock") Boolean hasNoLowStock;
	@QueryParam("generalId") String generalId;
	@QueryParam("uniqueId") String uniqueId;
	
	//TODO:: object specific fields, add to bson filter list
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.getName())) {
			filters.add(
				SearchUtils.getBasicSearchFilter("name", this.getName())
			);
		}
		if (this.getCategories() != null && !this.getStorageTypes().isEmpty()) {
			List<Bson> typeFilterList = new ArrayList<>(this.getStorageTypes().size());
			for (StorageType curType : this.getStorageTypes()) {
				typeFilterList.add(eq(
					"storageType",
					curType
				));
			}
			filters.add(Filters.or(typeFilterList));
		}
		if (this.getCategories() != null && !this.getCategories().isEmpty()) {
			List<Bson> catsFilterList = new ArrayList<>(this.getCategories().size());
			for (ObjectId curCategoryId : this.getCategories()) {
				catsFilterList.add(in(
					"categories",
					curCategoryId
				));
			}
			filters.add(Filters.or(catsFilterList));
		}
		if(this.hasValue(this.getInStorageBlocks())){
			filters.add(or(
				this.getInStorageBlocks().stream().map((ObjectId storageBlockId) -> {
					return in("storageBlocks", storageBlockId);
				}).toList()
			));
		}
		if(this.hasValue(this.getHasExpired())){
			if(this.getHasExpired()){
				filters.add(
					gt("stats.numExpired", 0)
				);
			}
		}
		if(this.hasValue(this.getHasNoExpired())){
			if(this.getHasNoExpired()){
				filters.add(
					eq("stats.numExpired", 0)
				);
			}
		}
		if(this.hasValue(this.getHasExpiryWarn())){
			if(this.getHasExpiryWarn()){
				filters.add(
					gt("stats.numExpiryWarn", 0)
				);
			}
		}
		if(this.hasValue(this.getHasNoExpiryWarn())){
			if(this.getHasNoExpiryWarn()){
				filters.add(
					eq("stats.numExpiryWarn", 0)
				);
			}
		}
		if(this.hasValue(this.getHasLowStock())){
			if(this.getHasLowStock()){
				filters.add(
					eq("stats.anyLowStock", true)
				);
			}
		}
		if(this.hasValue(this.getHasNoLowStock())){
			if(this.getHasNoLowStock()){
				filters.add(
					eq("stats.anyLowStock", true)
				);
			}
		}
		if(this.hasValue(this.getGeneralId())){
			filters.add(
				eq("generalIds.value", this.getGeneralId())
			);
		}
		if(this.hasValue(this.getUniqueId())){
			filters.add(
				eq("uniqueIds.value", this.getUniqueId())
			);
		}
		
		return filters;
	}
}
