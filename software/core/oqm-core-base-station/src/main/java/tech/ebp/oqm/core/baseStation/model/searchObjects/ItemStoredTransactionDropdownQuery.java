package tech.ebp.oqm.core.baseStation.model.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class ItemStoredTransactionDropdownQuery {
	@QueryParam("item") String item;
	@QueryParam("stored") String stored;
	
	@QueryParam("addButton") Boolean addButton = true;
	@QueryParam("checkinButton") Boolean checkinButton = true;
	@QueryParam("checkoutButton") Boolean checkoutButton = true;
	@QueryParam("setButton") Boolean setButton = true;
	@QueryParam("subtractButton") Boolean subtractButton = true;
	@QueryParam("transferButton") Boolean transferButton = true;
}
