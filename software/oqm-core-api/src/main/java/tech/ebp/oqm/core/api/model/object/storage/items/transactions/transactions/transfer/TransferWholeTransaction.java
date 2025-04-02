package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TransferWholeTransaction extends TransferTransaction {
	@Override
	public TransactionType getTransactionType() {
		return TransactionType.TRANSFER_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
	
	@NotNull
	@NonNull
	private ObjectId toBlock;
	
	/**
	 * The block we are pulling the whole stored from. Item must be either "BULK" or "AMOUNT UNIQUE".
	 */
	private ObjectId fromBlock;
	
	/**
	 * The specific stored object to move. Item must be either "AMOUNT_LIST" or "UNIQUE_MULTI".
	 */
	private ObjectId storedToTransfer;
}
