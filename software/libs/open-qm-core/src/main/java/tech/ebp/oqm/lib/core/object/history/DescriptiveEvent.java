package tech.ebp.oqm.lib.core.object.history;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

import javax.validation.constraints.NotNull;

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
