package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@ToString(callSuper = true)
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class HistorySearch extends SearchObject {
	
	@Setter
	@QueryParam("objectId")
	private String objectId;
	
	@QueryParam("eventType")
	private List<String> eventTypes;
	
	//TODO:: object specific fields, add to bson filter list
	//TODO:: Get hist in time range, etc
	
}
