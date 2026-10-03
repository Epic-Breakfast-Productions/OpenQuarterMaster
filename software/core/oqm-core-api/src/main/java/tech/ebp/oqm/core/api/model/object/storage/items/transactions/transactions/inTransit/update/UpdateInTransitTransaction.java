package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
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
@Schema(title = "UpdateInTransitTransaction", description = "A transaction to subtract a stored object.")
public class UpdateInTransitTransaction extends InTransitStoredTransaction {

	/**
	 * The specific stored object to subtract
	 *
	 * TODO:: ObjectNode? don't allow "from" updates
	 */
	@NotNull
	@NonNull
	private InTransit inTransitUpdates;

	@Override
	@Schema(constValue = "UPDATE_IN_TRANSIT", readOnly = true, required = true, examples = "UPDATE_IN_TRANSIT")
	public TransactionType getType() {
		return TransactionType.UPDATE_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
