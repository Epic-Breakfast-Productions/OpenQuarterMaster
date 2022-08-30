package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event for the creation of an object.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class CreateEvent extends HistoryEvent {
	
	private static EventType getClassType() {
		return EventType.CREATE;
	}
	
	public CreateEvent() {
		super(getClassType());
	}
	
	@Override
	public EventType getType() {
		return getClassType();
	}
}
