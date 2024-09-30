
package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

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
}
