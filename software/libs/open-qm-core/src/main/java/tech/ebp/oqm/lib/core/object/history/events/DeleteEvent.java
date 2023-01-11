package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.DescriptiveEvent;
import tech.ebp.oqm.lib.core.object.history.EventType;

/**
 * Event for the deletion of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class DeleteEvent extends DescriptiveEvent {
	
	@Override
	public EventType getType() {
		return EventType.DELETE;
	}
}
