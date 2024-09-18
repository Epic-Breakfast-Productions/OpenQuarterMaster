package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TransferAmountTransaction extends TransferTransaction {

	/**
	 * The amount we are transferring.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;

	/**
	 * If applicable, the specific stored item we are transferring from.
	 */
	private ObjectId fromStored;

	/**
	 * If applicable, the specific stored item we are transferring to.
	 */
	private ObjectId toStored;

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.TRANSFER_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
