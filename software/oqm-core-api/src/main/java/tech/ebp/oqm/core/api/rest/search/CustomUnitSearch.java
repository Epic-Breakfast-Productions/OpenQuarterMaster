package tech.ebp.oqm.core.api.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;

@ToString(callSuper = true)
@Getter
public class CustomUnitSearch extends SearchObject<CustomUnitEntry> {
	@QueryParam("name") String unitName;
	@QueryParam("unitSymbol") String unitSymbol;
	//TODO:: add to bson filter list
	
}
