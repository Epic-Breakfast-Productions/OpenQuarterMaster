package tech.ebp.oqm.baseStation.model.object.history.events.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class NewFileVersionEvent extends ObjectHistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.FILE_NEW_VERSION;
	}
}
