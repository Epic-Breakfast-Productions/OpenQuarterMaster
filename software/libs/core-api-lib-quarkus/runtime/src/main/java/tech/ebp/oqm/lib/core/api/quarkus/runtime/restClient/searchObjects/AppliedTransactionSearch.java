package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.PathParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AppliedTransactionSearch extends SearchKeyAttObject {
	public static AppliedTransactionSearch newInstance(){
		return new AppliedTransactionSearch();
	}

	@PathParam("itemId") String inventoryItemId;
}
