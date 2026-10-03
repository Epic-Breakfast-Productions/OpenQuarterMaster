package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "CancelInTransitAmountTransaction", description = "A transaction to cancel an amount of an in-transit stored.")
public class CancelInTransitAmountTransaction extends CancelInTransitTransaction {

	/**
	 * The amount we are canceling from this in transit stored.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;

	@Override
	@Schema(constValue = "CANCEL_IN_TRANSIT_AMOUNT", readOnly = true, required = true, examples = "CANCEL_IN_TRANSIT_AMOUNT")
	public TransactionType getType() {
		return TransactionType.CANCEL_IN_TRANSIT_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
