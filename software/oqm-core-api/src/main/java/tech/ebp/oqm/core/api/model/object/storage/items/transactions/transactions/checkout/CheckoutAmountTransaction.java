package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class CheckoutAmountTransaction extends CheckoutTransaction {

	/**
	 * If applicable, the specific stored object we are checking out from.
	 */
	private ObjectId fromStored;

	/**
	 * If applicable, the specific block we are checking out from. Use when checking out from bulk
	 */
	private ObjectId fromBlock;

	/**
	 * The amount we are checking out.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;


	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CHECKOUT_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
