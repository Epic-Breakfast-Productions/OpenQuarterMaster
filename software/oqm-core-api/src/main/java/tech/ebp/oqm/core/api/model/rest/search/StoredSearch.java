package tech.ebp.oqm.core.api.model.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@ToString(callSuper = true)
@Getter
@Setter
public class StoredSearch extends SearchKeyAttObject<Stored> {

	@PathParam("itemId") ObjectId inventoryItemId;
	@PathParam("blockId") ObjectId storageBlockId;
	@QueryParam("inStorageBlock") List<ObjectId> inStorageBlocks;
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
	
	//TODO:: object specific fields, add to bson filter list
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		if(this.hasValue(this.getInventoryItemId())){
			filters.add(eq("item", this.getInventoryItemId()));
		}
		if(this.hasValue(this.getStorageBlockId())){
			filters.add(eq("storageBlock", this.getStorageBlockId()));
		}

		//TODO::item
		//TODO:: redo these
//		if (this.hasValue(this.getItemBarcode())) {
//			filters.add(
//				eq("barcode", this.getItemBarcode())
//			);
//		}
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
