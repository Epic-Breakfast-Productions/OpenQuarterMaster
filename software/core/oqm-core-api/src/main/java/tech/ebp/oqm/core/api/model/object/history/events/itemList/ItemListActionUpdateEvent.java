package tech.ebp.oqm.core.api.model.object.history.events.itemList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
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
@Schema(title = "ItemListActionUpdateEvent", description = "An event describing the update of a list.")
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
	@Schema(constValue = "ITEM_LIST_ACTION_UPDATE", readOnly = true, required = true, examples = "ITEM_LIST_ACTION_UPDATE")
	public EventType getType() {
		return EventType.ITEM_LIST_ACTION_UPDATE;
	}

}
