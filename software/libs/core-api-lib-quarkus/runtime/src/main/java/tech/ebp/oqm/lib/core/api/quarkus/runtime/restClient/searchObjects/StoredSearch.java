package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@SuperBuilder
public class StoredSearch extends SearchKeyAttObject {

	@PathParam("itemId") String inventoryItemId;
	@PathParam("blockId") String storageBlockId;
	@QueryParam("inStorageBlock") List<String> inStorageBlocks;

	@QueryParam("hasExpiryDate") Boolean hasExpiryDate;
	@QueryParam("hasLowStockThreshold") Boolean hasLowStockThreshold;

	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;

}