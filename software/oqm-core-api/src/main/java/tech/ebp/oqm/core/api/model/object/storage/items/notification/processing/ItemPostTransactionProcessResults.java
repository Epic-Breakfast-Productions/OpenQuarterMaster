package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemPostTransactionProcessResults {
	@NonNull
	private ItemExpiryLowStockItemProcessResults expiryLowStockResults;

	@NonNull
	private ItemStoredStats stats;

}
