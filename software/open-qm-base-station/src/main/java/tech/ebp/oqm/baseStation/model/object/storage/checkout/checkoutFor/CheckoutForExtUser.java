package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutForType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutForExtUser extends CheckoutFor {
	@NonNull
	@NotNull
	@NotBlank
	private String externalId;
	
	@NonNull
	@NotNull
	private String name;
	
	//TODO:: determine from community what goes here
	
	@Override
	public tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutForType getType() {
		return CheckoutForType.EXT_SYS_USER;
	}
}
