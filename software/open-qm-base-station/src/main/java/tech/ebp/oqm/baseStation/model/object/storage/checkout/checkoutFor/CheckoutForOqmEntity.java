package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutForOqmEntity extends CheckoutFor {
	
	@NonNull
	@NotNull
	private ObjectId entity;
	
	public CheckoutForOqmEntity(InteractingEntity entity){
		this(entity.getId());
	}
	
	@Override
	public CheckoutForType getType() {
		return CheckoutForType.OQM_ENTITY;
	}
}
