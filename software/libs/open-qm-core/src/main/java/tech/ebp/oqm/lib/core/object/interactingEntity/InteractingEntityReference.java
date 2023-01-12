package tech.ebp.oqm.lib.core.object.interactingEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Data
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
public class InteractingEntityReference {
	
	@NotNull
	@NonNull
	private ObjectId entityId;
	
	@NotNull
	@NonNull
	private InteractingEntityType entityType;
}
