package tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

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
	public CheckoutForType getType() {
		return CheckoutForType.EXT_SYS_USER;
	}
}
