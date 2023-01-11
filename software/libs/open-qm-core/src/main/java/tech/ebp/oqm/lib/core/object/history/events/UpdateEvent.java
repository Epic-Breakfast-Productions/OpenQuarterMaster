package tech.ebp.oqm.lib.core.object.history.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Event for the update of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class UpdateEvent extends DescriptiveEvent {
	
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<String> fieldsUpdated = new ArrayList<>();
	
	
	@Override
	public EventType getType() {
		return EventType.UPDATE;
	}
}
