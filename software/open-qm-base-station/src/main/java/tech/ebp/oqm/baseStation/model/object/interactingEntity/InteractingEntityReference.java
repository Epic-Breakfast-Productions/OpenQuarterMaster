package tech.ebp.oqm.baseStation.model.object.interactingEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidInteractingEntityReference;

import jakarta.validation.constraints.NotNull;

/**
 * An identifier for an interacting entity.
 */
@Data
@Setter()
@AllArgsConstructor
@NoArgsConstructor
@ValidInteractingEntityReference
public class InteractingEntityReference {
	
	private ObjectId entityId;
	
	@NotNull
	@NonNull
	private InteractingEntityType entityType;
}
