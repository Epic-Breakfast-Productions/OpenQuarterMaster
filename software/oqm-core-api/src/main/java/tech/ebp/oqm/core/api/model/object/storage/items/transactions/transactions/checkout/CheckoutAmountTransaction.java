package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;

import javax.measure.Quantity;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CheckoutAmountTransaction extends CheckoutTransaction {

	/**
	 * If applicable, the specific stored object we are checking out from.
	 */
	private ObjectId fromStored;

	/**
	 * If applicable, the specific block we are checking out from. Use when checking out from bulk
	 * <p>
	 * Only specify when {@link CheckoutAmountTransaction#all} is `false`.
	 */
	private ObjectId fromBlock;

	/**
	 * The amount we are checking out.
	 */
	private Quantity<?> amount;
	
	/**
	 * Flag to specify to transfer all of what is in the source to the destination.
	 * <p>
	 * Only specify "true" if {@link CheckoutAmountTransaction#amount} is null.
	 */
	private boolean all = false;


	@Override
	public TransactionType getType() {
		return TransactionType.CHECKOUT_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
