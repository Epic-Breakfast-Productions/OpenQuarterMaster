
package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemExpiryLowStockProcessResults {

	@NonNull
	@NotNull
	private ObjectId itemId;

	/**
	 * Key is the id of the storage block it is stored in.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<ObjectId, List<StoredExpiryLowStockProcessResult>> results = new HashMap<>();
}
