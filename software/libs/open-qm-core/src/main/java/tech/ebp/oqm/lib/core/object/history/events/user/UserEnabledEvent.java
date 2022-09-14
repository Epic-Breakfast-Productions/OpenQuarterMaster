package tech.ebp.oqm.lib.core.object.history.events.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.history.events.EventType;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

/**
 * Event for the login action of a user.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class UserEnabledEvent extends HistoryEvent {
	
	private static EventType getClassType() {
		return EventType.USER_ENABLED;
	}
	
	public UserEnabledEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
