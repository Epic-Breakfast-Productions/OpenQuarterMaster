package com.ebp.openQuarterMaster.lib.core.history.events.user;

import com.ebp.openQuarterMaster.lib.core.history.events.EventType;
import com.ebp.openQuarterMaster.lib.core.history.events.HistoryEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event for the login action of a user.
 */
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
