package tech.ebp.oqm.core.api.model.object.history.events;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.DescriptiveEvent;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Event for the update of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
public class UpdateEvent extends DescriptiveEvent {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	public UpdateEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public UpdateEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}

	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<String> fieldsUpdated = new ArrayList<>();
	
	@Override
	public EventType getType() {
		return EventType.UPDATE;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
