package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

/**
 * Transaction to transfer an amount from one stored to another.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TransferAmountTransaction extends TransferTransaction {
	
	/**
	 * The amount we are transferring.
	 * <p>
	 * Only specify when {@link TransferAmountTransaction#all} is `false`.
	 */
	private Quantity<?> amount;
	
	/**
	 * Flag to specify to transfer all of what is in the source to the destination.
	 * <p>
	 * Only specify "true" if {@link TransferAmountTransaction#amount} is null.
	 */
	@lombok.Builder.Default
	private boolean all = false;
	
	/**
	 * If applicable, the storage block we are pulling the amount from. Used for BULK items.
	 */
	private ObjectId fromBlock;
	
	/**
	 * If applicable, the specific stored item we are transferring from. Used for AMOUNT_LIST items.
	 */
	private ObjectId fromStored;
	
	/**
	 * If applicable, the storage block we are putting the amount into. Used for BULK items.
	 */
	private ObjectId toBlock;
	
	/**
	 * If applicable, the specific stored item we are transferring to. Used for AMOUNT_LIST
	 */
	private ObjectId toStored;
	
	@Override
	public TransactionType getType() {
		return TransactionType.TRANSFER_AMOUNT;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
