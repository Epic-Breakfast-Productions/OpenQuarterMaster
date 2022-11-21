package tech.ebp.oqm.lib.core.object.history.events.externalService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.history.events.EventType;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ExtServiceSetupEvent extends HistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.EXT_SERVICE_SETUP;
	}
}
