package com.ebp.openQuarterMaster.lib.core.object.history.events.item;

import com.ebp.openQuarterMaster.lib.core.object.history.events.EventType;
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
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
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
