package tech.ebp.oqm.baseStation.model.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class ItemListActionUpdateEvent extends ObjectHistoryEvent {
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
