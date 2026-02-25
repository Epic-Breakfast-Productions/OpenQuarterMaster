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
@SuperBuilder(toBuilder = true)
public class StoredSearch extends SearchKeyAttObject {
	
	@QueryParam("itemId") String inventoryItemIdFromQuery;
	@QueryParam("blockId") String storageBlockIdFromQuery;

	@QueryParam("inStorageBlock") List<String> inStorageBlocks;

	@QueryParam("hasExpiryDate") Boolean hasExpiryDate;
	@QueryParam("hasLowStockThreshold") Boolean hasLowStockThreshold;
	
	@QueryParam("expired") Boolean hasExpired;
	@QueryParam("expiryWarn") Boolean hasExpiryWarn;
	@QueryParam("lowStock") Boolean hasLowStock;
	@QueryParam("identifier") List<String> identifiers;
}
