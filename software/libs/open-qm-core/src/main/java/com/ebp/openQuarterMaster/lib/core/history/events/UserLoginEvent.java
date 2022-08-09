package com.ebp.openQuarterMaster.lib.core.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class UserLoginEvent extends HistoryEvent {
	
	private static EventType getClassType() {
		return EventType.USER_LOGIN;
	}
	
	public UserLoginEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
