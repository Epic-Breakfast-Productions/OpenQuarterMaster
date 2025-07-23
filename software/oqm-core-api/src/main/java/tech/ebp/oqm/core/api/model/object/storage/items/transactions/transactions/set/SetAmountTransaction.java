package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

/**
 * transaction to set a specific amount within an amount stored.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SetAmountTransaction extends SetTransaction {
	
	/**
	 * The stored we are concerned with. Must be an amount stored.
	 * <p>
	 * In the case of a bulk item, can specify this or block.
	 */
	private ObjectId stored;
	
	/**
	 * In the case of a bulk item, the block we are concerned with.
	 */
	private ObjectId block;
	
	/**
	 * The amount to set the stored to.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;
	
	@Override
	public TransactionType getType() {
		return TransactionType.SET_AMOUNT;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
