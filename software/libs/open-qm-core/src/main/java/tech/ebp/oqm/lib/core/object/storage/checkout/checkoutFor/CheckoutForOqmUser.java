package tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutForOqmUser extends CheckoutFor {
	
	@NonNull
	@NotNull
	private ObjectId userId;
	
	@Override
	public CheckoutForType getType() {
		return CheckoutForType.OQM_USER;
	}
}
