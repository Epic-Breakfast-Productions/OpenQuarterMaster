package tech.ebp.oqm.lib.core.object.history.events.item;

import tech.ebp.oqm.lib.core.object.history.events.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

/**
 * Event for the subtraction of items from a storage block.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ItemSubEvent extends ItemAddSubEvent {
	
	@NonNull
	@NotNull
	private ObjectId storageBlockId;
	
	private static EventType getClassType() {
		return EventType.ITEM_SUBTRACT;
	}
	
	public ItemSubEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
