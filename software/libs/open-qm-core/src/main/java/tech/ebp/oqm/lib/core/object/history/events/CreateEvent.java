package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

/**
 * Event for the creation of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class CreateEvent extends ObjectHistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.CREATE;
	}
}
