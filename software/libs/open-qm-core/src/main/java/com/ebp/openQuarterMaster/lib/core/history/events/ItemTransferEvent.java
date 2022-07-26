package com.ebp.openQuarterMaster.lib.core.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class ItemTransferEvent extends ItemAddSubEvent {
	
	@NonNull
	@NotNull
	private ObjectId storageBlockFromId;
	@NonNull
	@NotNull
	private ObjectId storageBlockToId;
	
	private static EventType getClassType() {
		return EventType.ITEM_TRANSFER;
	}
	
	public ItemTransferEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
