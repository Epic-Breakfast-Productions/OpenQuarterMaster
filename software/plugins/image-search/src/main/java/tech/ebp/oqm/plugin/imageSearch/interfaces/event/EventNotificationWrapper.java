package tech.ebp.oqm.plugin.imageSearch.interfaces.event;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class EventNotificationWrapper {
	private String database;
	private String objectName;
	private String eventType;
	private ObjectNode event;
}
