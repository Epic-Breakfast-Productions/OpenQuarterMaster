package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;

@ToString(callSuper = true)
@Getter
public class CustomUnitSearch extends SearchObject<CustomUnitEntry> {
	@QueryParam("name") String unitName;
	@QueryParam("unitSymbol") String unitSymbol;
	//TODO:: add to bson filter list
	
	@Override
	public int getDefaultPageSize() {
		return 36;
	}
}