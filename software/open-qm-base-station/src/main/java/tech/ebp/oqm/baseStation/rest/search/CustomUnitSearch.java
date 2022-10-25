package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class CustomUnitSearch extends SearchObject<CustomUnitEntry> {
	@QueryParam("name") String unitName;
	@QueryParam("unitSymbol") String unitSymbol;
	//TODO:: add to bson filter list
	
	
	@HeaderParam("accept") String acceptHeaderVal;
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public int getDefaultPageSize() {
		return 36;
	}
}
