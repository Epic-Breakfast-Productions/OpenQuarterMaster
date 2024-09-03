package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract;

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
public class SubAmountTransaction extends SubtractTransaction {

	/**
	 * If applicable, the specific storage block we are adding to.
	 */
	private ObjectId fromStored;

	/**
	 * The amount we are adding.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SUBTRACT_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
