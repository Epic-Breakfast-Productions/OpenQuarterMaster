package tech.ebp.oqm.lib.core.object.interactingEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.validation.annotations.ValidInteractingEntityReference;

import javax.validation.constraints.NotNull;

/**
 * An identifier for an interacting entity.
 */
@Data
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
@ValidInteractingEntityReference
public class InteractingEntityReference {
	
	private ObjectId entityId;
	
	@NotNull
	@NonNull
	private InteractingEntityType entityType;
}
