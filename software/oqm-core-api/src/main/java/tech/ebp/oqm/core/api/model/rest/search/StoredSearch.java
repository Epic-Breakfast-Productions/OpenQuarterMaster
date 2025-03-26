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

	@PathParam("itemId") String inventoryItemIdFromPath;
	@QueryParam("itemId") String inventoryItemIdFromQuery;

	@PathParam("blockId") String storageBlockIdFromPath;
	@QueryParam("blockId") String storageBlockIdFromQuery;

	@QueryParam("inStorageBlock") List<ObjectId> inStorageBlocks;

	@QueryParam("hasExpiryDate") Boolean hasExpiryDate;
	@QueryParam("hasLowStockThreshold") Boolean hasLowStockThreshold;


	//TODO:: are these outdated?
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
	
	//TODO:: object specific fields, add to bson filter list
	
	
	public StoredSearch setInventoryItemId(String itemId){
		this.inventoryItemIdFromPath = itemId;
		return this;
	}
	public StoredSearch setStorageBlockId(String blockId){
		this.storageBlockIdFromPath = blockId;
		return this;
	}
	
	public String getInventoryItemId(){
		if(this.hasValue(this.getInventoryItemIdFromPath())){
			return this.getInventoryItemIdFromPath();
		} else if(this.hasValue(this.getInventoryItemIdFromQuery())){
			return this.getInventoryItemIdFromQuery();
		}
		return null;
	}
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		if(this.hasValue(this.getInventoryItemIdFromPath())){
			filters.add(eq("item", new ObjectId(this.getInventoryItemIdFromPath())));
		} else if(this.hasValue(this.getInventoryItemIdFromQuery())){
			filters.add(eq("item", new ObjectId(this.getInventoryItemIdFromQuery())));
		}
		
		if(this.hasValue(this.getStorageBlockIdFromPath())){
			filters.add(eq("storageBlock", new ObjectId(this.getStorageBlockIdFromPath())));
		} else if(this.hasValue(this.getStorageBlockIdFromQuery())){
			filters.add(eq("storageBlock", new ObjectId(this.getStorageBlockIdFromQuery())));
		}
		
		//TODO::item
		//TODO:: redo these
//		if (this.hasValue(this.getItemBarcode())) {
//			filters.add(
//				eq("barcode", this.getItemBarcode())
//			);
//		}
		if(this.hasValue(this.getInStorageBlocks())){
			if(this.getInStorageBlocks().size() == 1){
				filters.add(
					eq("storageBlock", this.getInStorageBlocks().getFirst())
				);
			} else {
				filters.add(or(
					this.getInStorageBlocks().stream().map((ObjectId storageBlockId) -> {
//					return exists("storageBlocks." + storageBlockId.toHexString());
						return eq("storageBlock", storageBlockId);
					}).toList()
				));
			}
		}
		if(this.hasValue(this.getHasExpiryDate())){
			filters.add(
				ne("expires", null)
			);
		}
		if(this.hasValue(this.getHasLowStockThreshold())){

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
