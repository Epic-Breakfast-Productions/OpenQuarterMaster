package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class SubWholeTransaction extends SubtractTransaction {

	/**
	 * The specific stored object to subtract
	 */
	private ObjectId toSubtract;

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SUBTRACT_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
