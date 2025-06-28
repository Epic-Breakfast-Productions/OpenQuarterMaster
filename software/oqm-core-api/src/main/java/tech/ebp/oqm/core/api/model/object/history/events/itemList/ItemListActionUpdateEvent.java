package tech.ebp.oqm.core.api.model.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder(toBuilder = true)
public class ItemListActionUpdateEvent extends ObjectHistoryEvent {
	public static final int CUR_SCHEMA_VERSION = 1;

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

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
