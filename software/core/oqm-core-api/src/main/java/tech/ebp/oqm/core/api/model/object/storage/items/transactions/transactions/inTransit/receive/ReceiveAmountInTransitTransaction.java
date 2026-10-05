package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive;

import jakarta.annotation.Nullable;
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
@Schema(title = "ReceiveAmountInTransitTransaction", description = "A transaction to subtract a stored object.")
public class ReceiveAmountInTransitTransaction extends ReceiveInTransitTransaction {

	/**
	 * Flag to specify to transfer all of what is in the source to the destination.
	 */
	@lombok.Builder.Default
	private boolean all = false;

	private Quantity<?> amount;

	@Nullable
	private ObjectId toStored;

	@Override
	@Schema(constValue = "RECEIVE_AMOUNT_IN_TRANSIT", readOnly = true, required = true, examples = "RECEIVE_AMOUNT_IN_TRANSIT")
	public TransactionType getType() {
		return TransactionType.RECEIVE_AMOUNT_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
