package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnPartCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Checkin transaction to mark the stored item as lost.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "CheckinLossTransaction", description = "A transaction to checkin a transaction as a loss.")
public class CheckinLossTransaction extends CheckinTransaction<ReturnPartCheckinDetails> {
	
	@Override
	@Schema(constValue = "CHECKIN_LOSS", readOnly = true, required = true, examples = "CHECKIN_LOSS")
	public TransactionType getType() {
		return TransactionType.CHECKIN_LOSS;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
