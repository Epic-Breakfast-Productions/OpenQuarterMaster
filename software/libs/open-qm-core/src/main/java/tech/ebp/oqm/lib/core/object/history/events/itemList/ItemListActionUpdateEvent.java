package tech.ebp.oqm.lib.core.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemListActionUpdateEvent extends UpdateEvent {
	public ItemListActionUpdateEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemListActionUpdateEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	private ObjectId itemId;
	private int actionIndex;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LIST_ACTION_UPDATE;
	}
}
