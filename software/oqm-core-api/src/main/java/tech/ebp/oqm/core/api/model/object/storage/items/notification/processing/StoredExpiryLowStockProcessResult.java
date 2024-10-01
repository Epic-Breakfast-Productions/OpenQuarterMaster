
package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryWarningEvent;

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

	public List<ItemExpiryLowStockEvent> getEvents(ObjectId itemId) {
		ArrayList<ItemExpiryLowStockEvent> output = new ArrayList<>();

		if (this.isExpired()) {
			output.add(ItemExpiredEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.entity()//TODO
				.build()
			);
		}
		if (this.isExpiryWarn()) {
			output.add(ItemExpiryWarningEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.entity()//TODO
				.build()
			);
		}
		if (this.isLowStock()) {
			output.add(ItemLowStockEvent.builder()
				.objectId(itemId)
				.storedId(this.storedId)
				.transactionId()//TODO
				.entity()//TODO
				.build()
			);
		}


		return output;
	}
}
