package tech.ebp.oqm.lib.core.object.history.events.externalService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class ExtServiceAuthEvent extends ObjectHistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.EXT_SERVICE_AUTH;
	}
}
