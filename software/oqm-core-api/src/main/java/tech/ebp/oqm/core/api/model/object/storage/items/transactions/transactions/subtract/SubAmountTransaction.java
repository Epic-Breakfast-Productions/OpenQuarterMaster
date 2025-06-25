package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;

import javax.measure.Quantity;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SubAmountTransaction extends SubtractTransaction {

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
