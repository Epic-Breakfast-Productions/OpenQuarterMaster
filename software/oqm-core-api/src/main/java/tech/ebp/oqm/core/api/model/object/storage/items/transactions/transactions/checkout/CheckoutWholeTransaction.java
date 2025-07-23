package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Transaction to checkout an entire stored item.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CheckoutWholeTransaction extends CheckoutTransaction {

	/**
	 * The specific stored object to check out.
	 */
	@NonNull
	@NotNull
	private ObjectId toCheckout;
	
	@Override
	public TransactionType getType() {
		return TransactionType.CHECKOUT_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
