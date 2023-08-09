package tech.ebp.oqm.baseStation.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.events.item.ItemAddSubEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Event for the transfer of items from a storage block to another.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class ItemTransferEvent extends ItemAddSubEvent {
	
	public ItemTransferEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemTransferEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	
	@NonNull
	@NotNull
	private ObjectId storageBlockFromId;
	@NonNull
	@NotNull
	private ObjectId storageBlockToId;
	
	@Override
	public EventType getType() {
		return EventType.ITEM_TRANSFER;
	}
}
