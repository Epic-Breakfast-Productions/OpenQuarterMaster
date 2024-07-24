package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class InteractingEntitySearch extends SearchObject {
	@QueryParam("name") String name;
	//TODO:: object specific fields

}
