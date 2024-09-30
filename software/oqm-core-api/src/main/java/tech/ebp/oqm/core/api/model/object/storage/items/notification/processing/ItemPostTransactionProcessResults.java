package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import lombok.*;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemPostTransactionProcessResults {
	@NonNull
	private ItemExpiryLowStockProcessResults expiryLowStockResults;
	@NonNull
	private ItemStoredStats stats;
}
