package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@ToString(callSuper = true)
@Getter
public class InventoryItemSearch extends SearchKeyAttObject<InventoryItem> {
	@QueryParam("name") String name;
	@QueryParam("itemBarcode") String itemBarcode;
	@QueryParam("itemCategories") List<ObjectId> categories;
	@QueryParam("inStorageBlock") List<ObjectId> inStorageBlocks;
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
	
	//TODO:: object specific fields, add to bson filter list
	
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
		if(this.hasValue(this.getInStorageBlocks())){
			filters.add(or(
				this.getInStorageBlocks().stream().map((ObjectId storageBlockId) -> {
					return exists("storageMap." + storageBlockId.toHexString());
				}).toList()
			));
		}
		if(this.hasValue(this.getHasExpired())){
			filters.add(
				(this.getHasExpired()?
					 ne(
						 "numExpired",
						 0
					 ) :
					 eq(
						 "numExpired",
						 0
					 )
				)
			);
		}
		if(this.hasValue(this.getHasExpiryWarn())){
			filters.add(
				(this.getHasExpiryWarn()?
					 ne(
						 "numExpiryWarn",
						 0
					 ) :
					 eq(
						 "numExpiryWarn",
						 0
					 )
				)
			);
		}
		if(this.hasValue(this.getHasLowStock())){
			filters.add(
				(this.getHasLowStock()?
					 ne(
						 "numLowStock",
						 0
					 ) :
					 eq(
						 "numLowStock",
						 0
					 )
				)
			);
		}
		
		return filters;
	}
}
