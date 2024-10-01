package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import lombok.*;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;

import java.util.List;

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
