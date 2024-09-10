package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class TransferWholeTransaction extends TransferTransaction {
	@Override
	public TransactionType getTransactionType() {
		return TransactionType.TRANSFER_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}

	/**
	 * The specific stored object to move.
	 */
	private ObjectId storedToTransfer;
}
