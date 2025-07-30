package tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
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
	public CheckoutForType getType() {
		return CheckoutForType.EXT_SYS_USER;
	}
}
