package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public class ItemLowStockEvent extends ObjectHistoryEvent {
	
	private ObjectId storageBlockId = null;
	
	private String identifier = null;
	
	private Integer index = null;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LOW_STOCK;
	}
}
