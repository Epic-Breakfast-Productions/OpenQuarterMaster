package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class ExternalServiceSearch extends SearchKeyAttObject<ExternalService> {
	@QueryParam("name") String serviceName;
	//TODO:: object specific fields, add to bson filter list
	
	
	@HeaderParam("accept") String acceptHeaderVal;
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public int getDefaultPageSize() {
		return 25;
	}
}
