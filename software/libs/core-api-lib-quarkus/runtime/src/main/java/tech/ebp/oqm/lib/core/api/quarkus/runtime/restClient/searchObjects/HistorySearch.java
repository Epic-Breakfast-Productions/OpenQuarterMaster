package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class HistorySearch extends SearchObject {
	
	@Setter
	@QueryParam("objectId")
	private String objectId;
	
	@QueryParam("eventType")
	private List<String> eventTypes;
	
	//TODO:: object specific fields, add to bson filter list
	//TODO:: Get hist in time range, etc
	
}
