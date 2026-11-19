package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitTransaction;

/**
 * Transaction to add a whole new stored object.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title = "AddWholeInTransitTransaction", description = "A transaction to add a stored that is in transit.")
public class AddWholeInTransitTransaction extends AddInTransitTransaction {

	/**
	 * The new stored object to add.
	 */
	@NonNull
	@NotNull
	private Stored toAdd;

	@Override
	@Schema(constValue = "ADD_WHOLE_IN_TRANSIT", readOnly = true, required = true, examples = "ADD_WHOLE_IN_TRANSIT")
	public TransactionType getType() {
		return TransactionType.ADD_WHOLE_IN_TRANSIT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
