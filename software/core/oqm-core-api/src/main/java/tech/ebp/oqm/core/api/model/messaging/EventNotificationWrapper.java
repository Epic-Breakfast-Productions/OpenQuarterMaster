package tech.ebp.oqm.core.api.model.messaging;

import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {
	private ObjectId database;
	private String objectName;//TODO:: change to objectType
	private ObjectHistoryEvent event;

	public EventType getEventType() {
		return event.getType();
	}
	public ObjectId getObjectId() {
		return event.getObjectId();
	}
}
