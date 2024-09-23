package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoredNotificationStatus {

	@lombok.Builder.Default
	private boolean expired = false;

	@lombok.Builder.Default
	private boolean expiredWarning = false;

	@lombok.Builder.Default
	private boolean lowStock = false;
}
