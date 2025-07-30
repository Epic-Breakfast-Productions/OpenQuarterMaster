package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add;

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
 * Transaction to add an amount.
 * <p>
 * Either adding an amount that gets converted to a new stored object, or to an existing stored object.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AddAmountTransaction extends AddTransaction {
	
	/**
	 * If applicable, the storage block we are adding to.
	 */
	private ObjectId toBlock;
	
	/**
	 * If applicable, the specific existing stored object we are adding to.
	 */
	private ObjectId toStored;
	
	/**
	 * The amount we are adding.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;
	
	@Override
	public TransactionType getType() {
		return TransactionType.ADD_AMOUNT;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
