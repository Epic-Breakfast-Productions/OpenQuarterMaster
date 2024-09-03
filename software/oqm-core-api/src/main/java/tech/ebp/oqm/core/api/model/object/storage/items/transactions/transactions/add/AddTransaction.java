package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class AddTransaction extends ItemStoredTransaction {

	/**
	 * The storage block we are adding to.
	 */
	@NotNull
	@NonNull
	private ObjectId toBlock;
}
