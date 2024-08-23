package tech.ebp.oqm.core.api.service.notification;

import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {
	private ObjectId database;
	private String objectName;
	private ObjectHistoryEvent event;
}
