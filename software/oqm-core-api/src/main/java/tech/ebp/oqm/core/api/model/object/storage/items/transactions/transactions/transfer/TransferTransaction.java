package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer;

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
public abstract class TransferTransaction extends ItemStoredTransaction {

	@NotNull
	@NonNull
	private ObjectId fromBlock;
	@NotNull
	@NonNull
	private ObjectId toBlock;

	/**
	 * If applicable, the specific stored item we are transferring from.
	 */
	private ObjectId fromStored;

	/**
	 * If applicable, the specific stored item we are transferring to.
	 */
	private ObjectId toStored;
}
