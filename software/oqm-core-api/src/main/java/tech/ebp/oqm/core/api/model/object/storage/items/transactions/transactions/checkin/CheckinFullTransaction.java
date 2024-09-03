package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class CheckinFullTransaction extends CheckinTransaction {
	@Override
	public TransactionType getTransactionType() {
		return TransactionType.CHECKIN_FULL;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
