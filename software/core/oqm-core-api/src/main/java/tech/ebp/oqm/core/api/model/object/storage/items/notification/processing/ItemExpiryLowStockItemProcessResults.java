
package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
public class ItemExpiryLowStockItemProcessResults implements ItemProcessResults {

	@NonNull
	@NotNull
	private ObjectId item;

	@NotNull
	@lombok.Builder.Default
	private boolean lowStock = false;

	/**
	 * Key is the id of the storage block it is stored in.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<ObjectId, List<StoredExpiryLowStockProcessResult>> results = new HashMap<>();

	@Override
	public List<ItemExpiryLowStockEvent> getEvents(ObjectId transactionId) {
		return this.getResults().values()
			.stream()
			.flatMap(curResultList->{
				return curResultList.stream().flatMap(curResult->{
					return curResult.getEvents(this.getItem(), transactionId).stream();
				});
			})
			.collect(Collectors.toList());
	}
}
