package tech.ebp.oqm.core.api.model.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
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

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public EventType getEventType() {
		return this.getEvent().getType();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public ObjectId getObjectId() {
		return this.getEvent().getObjectId();
	}
}
