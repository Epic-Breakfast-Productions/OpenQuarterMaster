package tech.ebp.oqm.core.api.model.object.history.events.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
@Schema(title = "NewFileVersionEvent", description = "An event describing when a file gets a new version.")
public class NewFileVersionEvent extends ObjectHistoryEvent {
	
	@Override
	@Schema(constValue = "FILE_NEW_VERSION", readOnly = true, required = true, examples = "FILE_NEW_VERSION")
	public EventType getType() {
		return EventType.FILE_NEW_VERSION;
	}
}
