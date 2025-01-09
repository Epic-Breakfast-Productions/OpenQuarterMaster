package tech.ebp.oqm.core.api.model.object.interactingEntity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidInteractingEntityReference;

/**
 * An identifier for an interacting entity.
 */
@Data
@Setter()
@AllArgsConstructor
@NoArgsConstructor
@ValidInteractingEntityReference
public class InteractingEntityReference {
	
	private ObjectId id;
	
	private String name;
	
	@NotNull
	@NonNull
	private InteractingEntityType type;
	
	public InteractingEntityReference(InteractingEntity entity) {
		this(entity.getId(), entity.getName(), entity.getInteractingEntityType());
	}
}
