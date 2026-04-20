package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Transaction to checkout an entire stored item.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title = "CheckoutWholeTransaction", description = "A transaction to checkout a whole stored object.")
public class CheckoutWholeTransaction extends CheckoutTransaction {

	/**
	 * The specific stored object to check out.
	 */
	@NonNull
	@NotNull
	private ObjectId toCheckout;
	
	@Override
	@Schema(constValue = "CHECKOUT_WHOLE", readOnly = true, required = true, examples = "CHECKOUT_WHOLE")
	public TransactionType getType() {
		return TransactionType.CHECKOUT_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
