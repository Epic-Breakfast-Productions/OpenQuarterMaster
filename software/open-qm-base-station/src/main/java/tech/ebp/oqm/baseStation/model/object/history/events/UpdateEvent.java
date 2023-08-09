package tech.ebp.oqm.baseStation.model.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.DescriptiveEvent;
import tech.ebp.oqm.baseStation.model.object.history.EventType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Event for the update of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class UpdateEvent extends DescriptiveEvent {
	
	public UpdateEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public UpdateEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	
	@NonNull
	@NotNull
	private List<String> fieldsUpdated = new ArrayList<>();
	
	
	@Override
	public EventType getType() {
		return EventType.UPDATE;
	}
}
