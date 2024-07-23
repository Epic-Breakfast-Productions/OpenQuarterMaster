package tech.ebp.oqm.core.api.service.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

@Data
@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {
	private ObjectId database;
	private String objectName;
	private ObjectHistoryEvent event;
}
