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
@SuperBuilder(toBuilder = true)
public class InventoryItemSearch extends SearchKeyAttObject {
	@QueryParam("name") String name;
	@QueryParam("storageTypes") List<String> storageTypes;
	@QueryParam("itemCategories") List<String> categories;
	@QueryParam("inStorageBlock") List<String> inStorageBlocks;
	@QueryParam("hasExpired") Boolean hasExpired;
	@QueryParam("hasNoExpired") Boolean hasNoExpired;
	@QueryParam("hasExpiryWarn") Boolean hasExpiryWarn;
	@QueryParam("hasNoExpiryWarn") Boolean hasNoExpiryWarn;
	@QueryParam("hasLowStock") Boolean hasLowStock;
	@QueryParam("hasNoLowStock") Boolean hasNoLowStock;
	@QueryParam("generalId") String generalId;
	@QueryParam("uniqueId") String uniqueId;
}
