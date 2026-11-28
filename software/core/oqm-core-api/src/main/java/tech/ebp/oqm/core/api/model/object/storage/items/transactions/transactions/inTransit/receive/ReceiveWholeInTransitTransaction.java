package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitStoredTransaction;

/**
 * Transaction to subtract entire stored item objects.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "ReceiveWholeInTransitTransaction", description = "A transaction to subtract a stored object.")
public class ReceiveWholeInTransitTransaction extends ReceiveInTransitTransaction {

	@Override
	@Schema(constValue = "RECEIVE_WHOLE_IN_TRANSIT", readOnly = true, required = true, examples = "RECEIVE_WHOLE_IN_TRANSIT")
	public TransactionType getType() {
		return TransactionType.RECEIVE_WHOLE_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
