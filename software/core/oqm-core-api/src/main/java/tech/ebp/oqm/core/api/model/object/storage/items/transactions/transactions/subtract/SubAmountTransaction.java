package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

/**
 * Transaction to subtract an amount from an item stored.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SubAmountTransaction extends SubtractTransaction {
	
	/**
	 * The storage block we are subtracting from
	 */
	private ObjectId fromBlock;
	
	/**
	 * If the specific storage block we are adding to.
	 */
	private ObjectId fromStored;
	
	/**
	 * The amount we are subtracting from.
	 * <p>
	 * Only specify when {@link SubAmountTransaction#all} is `false`.
	 */
	private Quantity<?> amount;
	
	/**
	 * Flag to specify to transfer all of what is in the source to the destination.
	 * <p>
	 * Only specify "true" if {@link SubAmountTransaction#amount} is null.
	 */
	@lombok.Builder.Default
	private boolean all = false;
	
	@Override
	public TransactionType getType() {
		return TransactionType.SUBTRACT_AMOUNT;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
