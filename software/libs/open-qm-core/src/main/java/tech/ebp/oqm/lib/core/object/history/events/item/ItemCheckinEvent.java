package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.validation.constraints.NotNull;

/**
 * Event for the addition of items to a storage block.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class ItemCheckinEvent extends ObjectHistoryEvent {
	
	public ItemCheckinEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemCheckinEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@NonNull
	@NotNull
	private ObjectId itemCheckoutId;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_CHECKOUT;
	}
}
