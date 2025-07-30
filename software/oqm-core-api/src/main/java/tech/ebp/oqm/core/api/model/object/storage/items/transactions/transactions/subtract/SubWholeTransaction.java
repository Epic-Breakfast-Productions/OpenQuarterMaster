package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Transaction to subtract entire stored item objects.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SubWholeTransaction extends SubtractTransaction {

	/**
	 * The specific stored object to subtract
	 */
	private ObjectId toSubtract;

	@Override
	public TransactionType getType() {
		return TransactionType.SUBTRACT_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
