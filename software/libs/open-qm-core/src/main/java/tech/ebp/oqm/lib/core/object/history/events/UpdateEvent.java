package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.DescriptiveEvent;
import tech.ebp.oqm.lib.core.object.history.EventType;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Event for the update of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class UpdateEvent extends DescriptiveEvent {
	
	@NonNull
	@NotNull
	private List<String> fieldsUpdated = new ArrayList<>();
	
	
	@Override
	public EventType getType() {
		return EventType.UPDATE;
	}
}
