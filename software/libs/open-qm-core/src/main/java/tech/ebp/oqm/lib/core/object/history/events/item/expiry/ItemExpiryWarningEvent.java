package tech.ebp.oqm.lib.core.object.history.events.item.expiry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.EventType;

/**
 * Event for the addition of items to a storage block.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public class ItemExpiryWarningEvent extends ItemExpiryEvent {
	
	@Override
	public EventType getType() {
		return EventType.ITEM_EXPIRY_WARNING;
	}
}
