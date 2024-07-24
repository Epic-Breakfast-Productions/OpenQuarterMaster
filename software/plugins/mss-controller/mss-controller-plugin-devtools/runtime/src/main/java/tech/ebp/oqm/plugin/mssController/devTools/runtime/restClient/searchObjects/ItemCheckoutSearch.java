package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class ItemCheckoutSearch extends SearchKeyAttObject {
	@QueryParam("item") String itemCheckedOut;
	@QueryParam("storageCheckedOutFrom") String storageCheckedOutFrom;
	@QueryParam("entity") String checkedOutBy;
	@QueryParam("stillCheckedOut") Boolean stillCheckedOut = true;
}
