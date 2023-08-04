package tech.ebp.oqm.baseStation.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

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
