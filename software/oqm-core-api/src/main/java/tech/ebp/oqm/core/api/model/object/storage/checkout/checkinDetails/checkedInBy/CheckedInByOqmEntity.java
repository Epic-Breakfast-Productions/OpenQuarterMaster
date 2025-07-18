package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CheckedInByOqmEntity extends CheckedInBy {
	
	@NonNull
	@NotNull
	private ObjectId entity;
	
	public CheckedInByOqmEntity(InteractingEntity entity){
		this(entity.getId());
	}
	
	@Override
	public CheckedInByType getType() {
		return CheckedInByType.OQM_ENTITY;
	}
}
