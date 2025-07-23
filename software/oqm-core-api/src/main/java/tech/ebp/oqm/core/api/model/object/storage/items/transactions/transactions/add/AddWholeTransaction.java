package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

/**
 * Transaction to add a whole new stored object.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class AddWholeTransaction extends AddTransaction {

	/**
	 * The storage block we are adding under.
	 */
	@NotNull
	@NonNull
	private ObjectId toBlock;
	
	/**
	 * The new stored object to add.
	 */
	@NonNull
	@NotNull
	private Stored toAdd;

	@Override
	public TransactionType getType() {
		return TransactionType.ADD_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
