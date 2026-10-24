package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
@Schema(title = "CancelInTransitWholeTransaction", description = "A transaction to cancel an in-transit stored.")
public class CancelInTransitWholeTransaction extends CancelInTransitTransaction {

	@Override
	@Schema(constValue = "CANCEL_IN_TRANSIT_WHOLE", readOnly = true, required = true, examples = "CANCEL_IN_TRANSIT_WHOLE")
	public TransactionType getType() {
		return TransactionType.CANCEL_IN_TRANSIT_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
