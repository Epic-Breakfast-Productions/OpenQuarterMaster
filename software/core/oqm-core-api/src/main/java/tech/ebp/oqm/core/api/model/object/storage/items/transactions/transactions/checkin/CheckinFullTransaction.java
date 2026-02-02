package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnFullCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Transaction to checkin the entirety of a checkout.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CheckinFullTransaction extends CheckinTransaction<ReturnFullCheckinDetails> {


	/**
	 * The block we are checking into
	 */
	private ObjectId toBlock;
	
	/**
	 * The stored item to put the amount into, if applicable.
	 */
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
