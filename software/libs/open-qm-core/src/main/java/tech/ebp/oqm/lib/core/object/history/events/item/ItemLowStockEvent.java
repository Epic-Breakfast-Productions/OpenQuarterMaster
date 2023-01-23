package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public class ItemLowStockEvent extends ObjectHistoryEvent {
	
	public ItemLowStockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemLowStockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	private ObjectId storageBlockId = null;
	
	private String identifier = null;
	
	private Integer index = null;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LOW_STOCK;
	}
}
