package tech.ebp.oqm.baseStation.model.object.history;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

/**
 * Describes an event with a description of the event.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public abstract class DescriptiveEvent extends ObjectHistoryEvent {
	
	public DescriptiveEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public DescriptiveEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	
	/** Description of the event */
	@NonNull
	@NotNull
	private String description = "";
	
	protected DescriptiveEvent(String description) {
		this.description = description;
	}
}
