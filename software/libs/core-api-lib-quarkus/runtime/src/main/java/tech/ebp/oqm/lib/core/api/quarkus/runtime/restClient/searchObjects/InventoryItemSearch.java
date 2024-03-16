package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

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
public class InventoryItemSearch extends SearchKeyAttObject {
	@QueryParam("name") String name;
	@QueryParam("itemBarcode") String itemBarcode;
	@QueryParam("itemCategories") List<String> categories;
	@QueryParam("inStorageBlock") List<String> inStorageBlocks;
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
}
