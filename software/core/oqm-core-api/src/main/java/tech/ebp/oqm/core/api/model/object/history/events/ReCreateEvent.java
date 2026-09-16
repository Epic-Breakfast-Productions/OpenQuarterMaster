package tech.ebp.oqm.core.api.model.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

/**
 * Event for the creation of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
@Schema(title = "ReCreateEvent", description = "An event describing the recreation of an object.")
public class ReCreateEvent extends ObjectHistoryEvent {

	public ReCreateEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}

	public ReCreateEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@Override
	@Schema(constValue = "RECREATE", readOnly = true, required = true, examples = "RECREATE")
	public EventType getType() {
		return EventType.RECREATE;
	}
}
