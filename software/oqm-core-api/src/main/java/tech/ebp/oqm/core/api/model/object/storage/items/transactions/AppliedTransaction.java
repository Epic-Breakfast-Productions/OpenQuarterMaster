package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AppliedTransaction extends AttKeywordMainObject {
	@Override
	public int getSchemaVersion() {
		return 1;
	}

	/**
	 * The interacting entity that performed the transaction.
	 */
	@NonNull
	@NotNull
	private ObjectId entity;

	/**
	 * The item that the action was performed on.
	 */
	@NonNull
	@NotNull
	private ObjectId inventoryItem;

	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	private ZonedDateTime timestamp = ZonedDateTime.now();

	/**
	 * The transaction that occurred
	 */
	@NotNull
	@NonNull
	private ItemStoredTransaction transaction;
}
