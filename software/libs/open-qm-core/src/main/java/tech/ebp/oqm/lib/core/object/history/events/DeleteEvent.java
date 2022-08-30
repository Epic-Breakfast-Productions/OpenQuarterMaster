package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event for the deletion of an object.
 */
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
