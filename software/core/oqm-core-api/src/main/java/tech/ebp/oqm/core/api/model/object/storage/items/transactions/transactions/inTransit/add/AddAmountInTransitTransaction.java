package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add;

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
 * Transaction to add an amount.
 * <p>
 * Either adding an amount that gets converted to a new stored object, or to an existing stored object.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "AddAmountInTransitTransaction", description = "A transaction to add an amount to a stored object.")
public class AddAmountInTransitTransaction extends AddInTransitTransaction {

	/**
	 * The amount we are adding.
	 */
	@NonNull
	@NotNull
	private Quantity<?> amount;

	@Override
	@Schema(constValue = "ADD_AMOUNT_IN_TRANSIT", readOnly = true, required = true, examples = "ADD_AMOUNT_IN_TRANSIT")
	public TransactionType getType() {
		return TransactionType.ADD_AMOUNT_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
