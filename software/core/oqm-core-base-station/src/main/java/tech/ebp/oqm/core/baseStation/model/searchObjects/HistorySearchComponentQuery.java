package tech.ebp.oqm.core.baseStation.model.searchObjects;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class HistorySearchComponentQuery {
	@QueryParam("id") String id;
	@QueryParam("objectUrl") String objectUrl;

	@QueryParam("accordion")
	@DefaultValue("false")
	boolean accordion;


}
