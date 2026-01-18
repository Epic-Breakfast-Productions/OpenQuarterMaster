package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

/**
 * Transaction to checkout an amount of item stored.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
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
	@lombok.Builder.Default
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
