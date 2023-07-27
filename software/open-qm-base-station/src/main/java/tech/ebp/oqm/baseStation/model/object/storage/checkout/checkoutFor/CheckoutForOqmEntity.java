package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutForType;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutForOqmEntity extends CheckoutFor {
	
	@NonNull
	@NotNull
	private InteractingEntityReference entity;
	
	@Override
	public CheckoutForType getType() {
		return CheckoutForType.OQM_ENTITY;
	}
}
