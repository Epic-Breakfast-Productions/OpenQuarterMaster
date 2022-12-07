package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.EventType;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ItemLowStockEvent extends ItemStorageBlockEvent {
	
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LOW_STOCK;
	}
}
