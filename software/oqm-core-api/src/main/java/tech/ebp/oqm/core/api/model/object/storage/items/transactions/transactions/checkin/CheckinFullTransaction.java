package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnFullCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CheckinFullTransaction extends CheckinTransaction<ReturnFullCheckinDetails> {


	/**
	 * The block we are checking into
	 */
	private ObjectId toBlock;

	private ObjectId toStored;

	@Override
	public TransactionType getType() {
		return TransactionType.CHECKIN_FULL;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
