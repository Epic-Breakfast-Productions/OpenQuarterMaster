package tech.ebp.oqm.lib.core.object.history.events.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.history.events.EventType;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

/**
 * Event for the login action of a user.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class UserDisabledEvent extends HistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.USER_DISABLED;
	}
}
