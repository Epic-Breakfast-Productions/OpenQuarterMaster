package com.ebp.openQuarterMaster.lib.core.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class DeleteEvent extends DescriptiveEvent {
	
	private static EventType getClassType() {
		return EventType.DELETE;
	}
	
	public DeleteEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
