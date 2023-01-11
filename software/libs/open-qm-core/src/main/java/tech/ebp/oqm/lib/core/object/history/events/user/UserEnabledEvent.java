package tech.ebp.oqm.lib.core.object.history.events.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

/**
 * Event for the login action of a user.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class UserEnabledEvent extends ObjectHistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.USER_ENABLED;
	}
}
