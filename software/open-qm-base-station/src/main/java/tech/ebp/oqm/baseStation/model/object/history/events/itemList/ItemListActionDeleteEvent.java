package tech.ebp.oqm.baseStation.model.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class ItemListActionDeleteEvent extends DeleteEvent {
	public ItemListActionDeleteEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemListActionDeleteEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	private ObjectId itemId;
	private int actionIndex;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_LIST_ACTION_DELETE;
	}
}
