package tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class CheckoutForExtUser extends CheckoutFor {
	@NonNull
	@NotNull
	private String externalId;
	
	private String name;
	
	//TODO:: determine from community what goes here
	
	@Override
	public CheckoutForType getType() {
		return CheckoutForType.EXT_SYS_USER;
	}
}
