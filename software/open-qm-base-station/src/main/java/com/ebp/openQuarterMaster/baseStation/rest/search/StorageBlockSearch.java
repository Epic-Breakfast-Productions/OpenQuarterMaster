package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;

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
	
	
	//TODO:: add to bson filter list
}
