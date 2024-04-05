package tech.ebp.oqm.core.api.model.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class ItemListActionAddEvent extends ObjectHistoryEvent {
	public ItemListActionAddEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemListActionAddEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	private ObjectId itemId;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LIST_ACTION_ADD;
	}
}
