package tech.ebp.oqm.core.api.model.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

/**
 * The event wrapper that gets put around an event before it is sent as a message on the event outbound queue.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {

	/**
	 * The database this event is concerning
	 */
	private ObjectId database;
	/**
	 * The object type which this event is about
	 */
	private String objectType;
	/**
	 * The actual event
	 */
	private ObjectHistoryEvent event;

	/**
	 * Convenience wrapper to get the event type.
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public EventType getEventType() {
		return this.getEvent().getType();
	}

	/**
	 * Convenience wrapper to get the event's object id.
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public ObjectId getObjectId() {
		return this.getEvent().getObjectId();
	}
}
