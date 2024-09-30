package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpiryLowStockProcessResults {

	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<ObjectId, ItemExpiryLowStockProcessResults> itemResults = new HashMap<>();
}
