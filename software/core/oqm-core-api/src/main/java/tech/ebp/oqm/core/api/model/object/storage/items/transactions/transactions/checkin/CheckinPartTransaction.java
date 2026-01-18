package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnPartCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Checkin transaction to only checkin a part of the amount checked out.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CheckinPartTransaction extends CheckinTransaction<ReturnPartCheckinDetails> {
	
	@Override
	public TransactionType getType() {
		return TransactionType.CHECKIN_PART;
	}
	
	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
