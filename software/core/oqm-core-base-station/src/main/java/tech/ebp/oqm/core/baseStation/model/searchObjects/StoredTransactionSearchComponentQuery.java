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
public class StoredTransactionSearchComponentQuery {
	@QueryParam("id") String id;
	@QueryParam("item") String item;
	@QueryParam("storedItem") String storedItem;

	@QueryParam("accordion")
	@DefaultValue("false")
	boolean accordion;

}
