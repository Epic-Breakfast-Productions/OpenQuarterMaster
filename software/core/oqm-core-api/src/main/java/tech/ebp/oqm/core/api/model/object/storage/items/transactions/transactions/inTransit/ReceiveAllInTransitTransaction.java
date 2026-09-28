package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubtractTransaction;

/**
 * Transaction to subtract entire stored item objects.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "SubWholeTransaction", description = "A transaction to subtract a stored object.")
public class ReceiveAllInTransitTransaction extends InTransitTransaction {

	/**
	 * The specific stored object to subtract
	 */
	private ObjectId toSubtract;

	@Override
	@Schema(constValue = "SUBTRACT_WHOLE", readOnly = true, required = true, examples = "SUBTRACT_WHOLE")
	public TransactionType getType() {
		return TransactionType.RECEIVE_ALL_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
