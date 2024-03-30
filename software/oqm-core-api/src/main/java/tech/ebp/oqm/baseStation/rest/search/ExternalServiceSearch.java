package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;

@ToString(callSuper = true)
@Getter
public class ExternalServiceSearch extends SearchKeyAttObject<ExternalService> {
	@QueryParam("name") String serviceName;
	//TODO:: object specific fields, add to bson filter list
	
}
