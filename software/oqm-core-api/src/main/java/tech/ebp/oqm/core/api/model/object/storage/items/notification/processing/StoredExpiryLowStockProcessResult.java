
package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoredExpiryLowStockProcessResult {

	@NotNull
	@NonNull
	private ObjectId storedId;

	@NotNull
	@lombok.Builder.Default
	private boolean expired = false;
	@NotNull
	@lombok.Builder.Default
	private boolean expiryWarn = false;
	@NotNull
	@lombok.Builder.Default
	private boolean lowStock = false;

	public List<ItemExpiryLowStockEvent> getEvents(ObjectId itemId, ObjectId transactionId) {
		ArrayList<ItemExpiryLowStockEvent> output = new ArrayList<>();

		if (this.isExpired()) {
			output.add(ItemExpiredEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.build()
			);
		}
		if (this.isExpiryWarn()) {
			output.add(ItemExpiryWarningEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.build()
			);
		}
		if (this.isLowStock()) {
			output.add(ItemLowStockEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.transactionId(transactionId)
				.build()
			);
		}

		return output;
	}
}
