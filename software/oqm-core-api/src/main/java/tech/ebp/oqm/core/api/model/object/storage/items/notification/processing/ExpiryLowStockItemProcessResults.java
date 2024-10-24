package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpiryLowStockItemProcessResults implements ItemProcessResults {

	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<ObjectId, ItemExpiryLowStockItemProcessResults> itemResults = new HashMap<>();

	@Override
	public List<ItemExpiryLowStockEvent> getEvents(ObjectId transactionId) {
		return this.itemResults.values().stream()
			.flatMap(list->list.getEvents(transactionId).stream())
			.toList();
	}
}
