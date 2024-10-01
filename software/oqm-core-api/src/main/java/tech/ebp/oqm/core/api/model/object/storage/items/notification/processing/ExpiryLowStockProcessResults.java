package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpiryLowStockProcessResults implements ProcessResults {

	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<ObjectId, ItemExpiryLowStockProcessResults> itemResults = new HashMap<>();

	@Override
	public List<ItemExpiryLowStockEvent> getEvents() {
		return this.itemResults.values().stream()
			.flatMap(list->list.getEvents().stream())
			.toList();
	}
}
