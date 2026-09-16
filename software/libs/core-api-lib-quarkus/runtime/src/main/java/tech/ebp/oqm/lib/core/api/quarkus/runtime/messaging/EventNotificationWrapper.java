package tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A convenience object to use when reading history event notifications.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class EventNotificationWrapper {
	/** The database the event occurred in. */
	private String database;
	/** The name of the object that was affected. */
	private String objectName;
	/** The type of event that occurred. */
	private String eventType;
	/** The id of the object that was affected. */
	private String objectId;
	/** The event data. */
	private ObjectNode event;
}
