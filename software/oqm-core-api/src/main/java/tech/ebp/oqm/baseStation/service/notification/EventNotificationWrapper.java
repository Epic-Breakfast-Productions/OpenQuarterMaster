package tech.ebp.oqm.baseStation.service.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

@Data
@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {
	private String objectName;
	private ObjectHistoryEvent event;
}
