package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

/**
 * Transaction to subtract entire stored item objects.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "StoredToInTransitAmountTransaction", description = "A transaction to subtract a stored object.")
public class StoredToInTransitAmountTransaction extends StoredToInTransitTransaction {

	/**
	 * Flag to specify to transfer all of what is in the source to the destination.
	 */
	@lombok.Builder.Default
	private boolean all = false;

	private Quantity<?> amount;

	private ObjectId subtractFromStored;

	private ObjectId toInTransitStored;

	@Override
	@Schema(constValue = "STORED_TO_IN_TRANSIT_AMOUNT", readOnly = true, required = true, examples = "STORED_TO_IN_TRANSIT_AMOUNT")
	public TransactionType getType() {
		return TransactionType.STORED_TO_IN_TRANSIT_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
