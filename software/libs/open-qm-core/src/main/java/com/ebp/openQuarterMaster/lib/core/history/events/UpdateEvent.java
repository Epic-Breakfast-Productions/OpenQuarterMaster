package com.ebp.openQuarterMaster.lib.core.history.events;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class UpdateEvent extends DescriptiveEvent {
	//	private JsonNode updateJson;
	
	private static EventType getClassType() {
		return EventType.UPDATE;
	}
	
	public UpdateEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
