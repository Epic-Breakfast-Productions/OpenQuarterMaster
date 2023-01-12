package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

import javax.validation.constraints.NotNull;

/**
 * Event for the addition of items to a storage block.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class ItemAddEvent extends ItemAddSubEvent {
	
	public ItemAddEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemAddEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@NonNull
	@NotNull
	private ObjectId storageBlockId;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_ADD;
	}
}
