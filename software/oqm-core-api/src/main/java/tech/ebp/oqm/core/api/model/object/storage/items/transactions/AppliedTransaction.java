package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemPostTransactionProcessResults;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.StoredStats;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
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
	 * The item that the action was performed on.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private LinkedHashSet<ObjectId> affectedStored = new LinkedHashSet<>();

	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime timestamp = ZonedDateTime.now();

	/**
	 * The transaction that occurred
	 */
	@NotNull
	@NonNull
	private ItemStoredTransaction transaction;

	@NonNull
	@NotNull
	private ItemPostTransactionProcessResults postApplyResults;
}